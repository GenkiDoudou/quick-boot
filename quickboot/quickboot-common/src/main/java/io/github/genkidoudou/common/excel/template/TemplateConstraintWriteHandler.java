package io.github.genkidoudou.common.excel.template;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 导入模板列约束写入：按 {@link ExcelDictFormat} 生成下拉，按 Validation 生成输入提示。
 *
 * <p>严格度：提示为主（showPromptBox），不强制拒绝非法输入。
 */
public class TemplateConstraintWriteHandler implements SheetWriteHandler {

  private static final Logger log = LoggerFactory.getLogger(TemplateConstraintWriteHandler.class);

  /** Excel 显式列表公式长度软上限；超出则改用隐藏 sheet。 */
  static final int EXPLICIT_LIST_CHAR_LIMIT = 255;

  private final Class<?> headClass;
  private final int firstDataRow;
  private final int lastDataRow;

  /**
   * @param headClass EasyExcel 行模型（通常为 ImportRow）
   */
  public TemplateConstraintWriteHandler(Class<?> headClass) {
    this(headClass, 1, 2000);
  }

  /**
   * @param headClass    行模型
   * @param firstDataRow 数据起始行（0-based，表头下一行通常为 1）
   * @param lastDataRow  数据结束行（含）
   */
  public TemplateConstraintWriteHandler(Class<?> headClass, int firstDataRow, int lastDataRow) {
    this.headClass = headClass;
    this.firstDataRow = Math.max(1, firstDataRow);
    this.lastDataRow = Math.max(this.firstDataRow, lastDataRow);
  }

  @Override
  public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
    if (headClass == null) {
      return;
    }
    Sheet sheet = writeSheetHolder.getSheet();
    Workbook workbook = writeWorkbookHolder.getWorkbook();
    DataValidationHelper helper = sheet.getDataValidationHelper();
    List<ExcelPropertyColumn> columns = ExcelPropertyColumnScanner.scan(headClass);

    for (ExcelPropertyColumn column : columns) {
      Field field = column.field();
      int col = column.columnIndex();
      ExcelDictFormat dictFormat = field.getAnnotation(ExcelDictFormat.class);
      DictLabelResolver.ResolveResult dictResult = DictLabelResolver.resolve(dictFormat, field.getName());

      String prompt = ValidationPromptBuilder.build(field);
      if (dictResult.skippedWithWarn() && StringUtils.isNotBlank(dictResult.warnMessage())) {
        prompt = appendPrompt(prompt, dictResult.warnMessage());
      }
      if (dictResult.hasLabels()) {
        String sep = dictFormat != null ? dictFormat.separator() : ",";
        if (StringUtils.isNotEmpty(sep)) {
          prompt = appendPrompt(prompt, "多值请用分隔符「" + sep + "」拼接标签");
        }
        prompt = appendPrompt(prompt, "请从下拉选择");
        addDropdown(workbook, sheet, helper, col, dictResult.labels(), prompt, field.getName());
      } else if (StringUtils.isNotBlank(prompt)) {
        addPromptOnly(sheet, helper, col, prompt);
      }

      Integer maxLen = ValidationPromptBuilder.resolveMaxLength(field);
      if (maxLen != null && maxLen > 0) {
        addTextLengthSoft(sheet, helper, col, maxLen);
      }
    }
  }

  private void addDropdown(Workbook workbook,
                           Sheet sheet,
                           DataValidationHelper helper,
                           int col,
                           List<String> labels,
                           String prompt,
                           String fieldName) {
    String joined = String.join(",", labels);
    DataValidationConstraint constraint;
    if (joined.length() > EXPLICIT_LIST_CHAR_LIMIT) {
      log.warn("下拉选项过长，改用隐藏 sheet: field={}, chars={}", fieldName, joined.length());
      constraint = createHiddenSheetConstraint(workbook, helper, col, labels, fieldName);
      if (constraint == null) {
        log.warn("隐藏 sheet 下拉创建失败，降级为仅提示: field={}", fieldName);
        addPromptOnly(sheet, helper, col, appendPrompt(prompt, "选项较多，请按字典标签填写"));
        return;
      }
    } else {
      constraint = helper.createExplicitListConstraint(labels.toArray(new String[0]));
    }
    CellRangeAddressList addressList = new CellRangeAddressList(firstDataRow, lastDataRow, col, col);
    DataValidation validation = helper.createValidation(constraint, addressList);
    applySoftUi(validation, prompt);
    sheet.addValidationData(validation);
  }

  private DataValidationConstraint createHiddenSheetConstraint(Workbook workbook,
                                                               DataValidationHelper helper,
                                                               int col,
                                                               List<String> labels,
                                                               String fieldName) {
    try {
      String sheetName = "_tpl_dict_" + col;
      Sheet hidden = workbook.getSheet(sheetName);
      if (hidden == null) {
        hidden = workbook.createSheet(sheetName);
        int hiddenIndex = workbook.getSheetIndex(hidden);
        workbook.setSheetHidden(hiddenIndex, true);
      }
      for (int i = 0; i < labels.size(); i++) {
        Row row = hidden.getRow(i);
        if (row == null) {
          row = hidden.createRow(i);
        }
        row.createCell(0).setCellValue(labels.get(i));
      }
      String nameName = "tpl_dict_col_" + col + "_" + Math.abs(fieldName.hashCode());
      Name named = workbook.getName(nameName);
      if (named == null) {
        named = workbook.createName();
        named.setNameName(nameName);
      }
      named.setRefersToFormula("'" + sheetName + "'!$A$1:$A$" + labels.size());
      return helper.createFormulaListConstraint(nameName);
    } catch (Exception e) {
      log.warn("创建隐藏 sheet 下拉失败: field={}, err={}", fieldName, e.getMessage());
      return null;
    }
  }

  private void addPromptOnly(Sheet sheet, DataValidationHelper helper, int col, String prompt) {
    // 恒真公式：仅挂载输入提示，不硬拦
    DataValidationConstraint constraint = helper.createCustomConstraint("TRUE");
    CellRangeAddressList addressList = new CellRangeAddressList(firstDataRow, lastDataRow, col, col);
    DataValidation validation = helper.createValidation(constraint, addressList);
    applySoftUi(validation, prompt);
    sheet.addValidationData(validation);
  }

  private void addTextLengthSoft(Sheet sheet, DataValidationHelper helper, int col, int maxLen) {
    DataValidationConstraint constraint = helper.createTextLengthConstraint(
      DataValidationConstraint.OperatorType.LESS_OR_EQUAL,
      String.valueOf(maxLen),
      null);
    CellRangeAddressList addressList = new CellRangeAddressList(firstDataRow, lastDataRow, col, col);
    DataValidation validation = helper.createValidation(constraint, addressList);
    // 长度仍用提示模式：不弹错误框
    applySoftUi(validation, "长度不可超过 " + maxLen);
    if (validation instanceof XSSFDataValidation) {
      validation.setShowErrorBox(false);
    }
    sheet.addValidationData(validation);
  }

  private static void applySoftUi(DataValidation validation, String prompt) {
    validation.setSuppressDropDownArrow(true);
    validation.setShowErrorBox(false);
    if (StringUtils.isNotBlank(prompt)) {
      validation.setShowPromptBox(true);
      String title = "填写说明";
      String text = prompt.length() > 255 ? prompt.substring(0, 252) + "..." : prompt;
      validation.createPromptBox(title, text);
    }
    if (validation instanceof XSSFDataValidation) {
      validation.setSuppressDropDownArrow(true);
      validation.setShowErrorBox(false);
    }
  }

  private static String appendPrompt(String base, String extra) {
    if (StringUtils.isBlank(extra)) {
      return base;
    }
    if (StringUtils.isBlank(base)) {
      return extra;
    }
    return base + "；" + extra;
  }
}
