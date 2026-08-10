package io.github.genkidoudou.common.excel.template;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import io.github.genkidoudou.common.excel.dict.DictLookup;
import io.github.genkidoudou.common.excel.dict.DictLookupHolder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 导入模板列约束：扫描、提示、下拉与降级路径最小验证。
 */
class TemplateConstraintWriteHandlerTest {

  @AfterEach
  void tearDown() {
    DictLookupHolder.clear();
  }

  @Test
  void scanner_declarationOrder_whenNoIndex() {
    List<ExcelPropertyColumn> cols = ExcelPropertyColumnScanner.scan(SampleImportRow.class);
    assertEquals(3, cols.size());
    assertEquals("userName", cols.get(0).field().getName());
    assertEquals(0, cols.get(0).columnIndex());
    assertEquals("sex", cols.get(1).field().getName());
    assertEquals(1, cols.get(1).columnIndex());
    assertEquals("phonenumber", cols.get(2).field().getName());
    assertEquals(2, cols.get(2).columnIndex());
  }

  @Test
  void dictLabels_inline() {
    ExcelDictFormat format = SampleImportRow.class.getDeclaredFields()[1].getAnnotation(ExcelDictFormat.class);
    DictLabelResolver.ResolveResult result = DictLabelResolver.resolve(format, "sex");
    assertTrue(result.hasLabels());
    assertEquals(List.of("男", "女"), result.labels());
    assertFalse(result.skippedWithWarn());
  }

  @Test
  void dictLabels_lookupMissing_skipsWithWarn() {
    ExcelDictFormat format = DictTypeRow.class.getDeclaredFields()[0].getAnnotation(ExcelDictFormat.class);
    DictLabelResolver.ResolveResult result = DictLabelResolver.resolve(format, "status");
    assertFalse(result.hasLabels());
    assertTrue(result.skippedWithWarn());
  }

  @Test
  void dictLabels_lookupPresent() {
    DictLookupHolder.set(new DictLookup() {
      @Override
      public String getLabel(String dictType, String value) {
        return null;
      }

      @Override
      public String getValue(String dictType, String label) {
        return null;
      }

      @Override
      public List<String> listLabels(String dictType) {
        return List.of("正常", "停用");
      }
    });
    ExcelDictFormat format = DictTypeRow.class.getDeclaredFields()[0].getAnnotation(ExcelDictFormat.class);
    DictLabelResolver.ResolveResult result = DictLabelResolver.resolve(format, "status");
    assertEquals(List.of("正常", "停用"), result.labels());
  }

  @Test
  void validationPrompt_order() throws Exception {
    var field = SampleImportRow.class.getDeclaredField("phonenumber");
    String prompt = ValidationPromptBuilder.build(field);
    assertNotNull(prompt);
    assertTrue(prompt.contains("手机号"));
    assertTrue(prompt.contains("1[3-9]"));
  }

  @Test
  void write_withConstraints_hasValidations() throws Exception {
    byte[] bytes = write(SampleImportRow.class, true);
    try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      Sheet sheet = workbook.getSheetAt(0);
      List<? extends DataValidation> validations = sheet.getDataValidations();
      assertFalse(validations.isEmpty(), "应写入数据有效性/提示");
      boolean hasList = validations.stream().anyMatch(v -> {
        String[] list = v.getValidationConstraint().getExplicitListValues();
        return list != null && list.length >= 2;
      });
      assertTrue(hasList, "性别列应有显式下拉");
    }
  }

  @Test
  void write_withoutHandler_noValidations() throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    EasyExcel.write(os, SampleImportRow.class)
      .sheet("t")
      .doWrite(Collections.emptyList());
    try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(os.toByteArray()))) {
      assertTrue(workbook.getSheetAt(0).getDataValidations().isEmpty());
    }
  }

  @Test
  void write_oversizedViaLookup_degradesSafely() throws Exception {
    List<String> labels = new ArrayList<>();
    for (int i = 0; i < 40; i++) {
      labels.add("选项标签编号" + i + "_填充字符填充字符填充");
    }
    DictLookupHolder.set(new DictLookup() {
      @Override
      public String getLabel(String dictType, String value) {
        return null;
      }

      @Override
      public String getValue(String dictType, String label) {
        return null;
      }

      @Override
      public List<String> listLabels(String dictType) {
        return labels;
      }
    });
    assertTrue(String.join(",", labels).length() > TemplateConstraintWriteHandler.EXPLICIT_LIST_CHAR_LIMIT);

    byte[] bytes = write(DictTypeRow.class, true);
    try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      Sheet sheet = workbook.getSheetAt(0);
      assertFalse(sheet.getDataValidations().isEmpty());
      boolean hasHidden = false;
      for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
        if (workbook.isSheetHidden(i) || workbook.isSheetVeryHidden(i)) {
          hasHidden = true;
          break;
        }
      }
      assertTrue(hasHidden, "超长下拉应使用隐藏 sheet");
    }
  }

  private static byte[] write(Class<?> head, boolean constraints) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    var writer = EasyExcel.write(os, head).sheet("t");
    if (constraints) {
      writer.registerWriteHandler(new TemplateConstraintWriteHandler(head));
    }
    writer.doWrite(Collections.emptyList());
    return os.toByteArray();
  }

  public static class SampleImportRow {
    @NotBlank(message = "用户账号不能为空")
    @ExcelProperty("用户账号")
    private String userName;

    @ExcelDictFormat(dictText = {"0=男", "1=女"})
    @ExcelProperty("性别")
    private String sex;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @ExcelProperty("手机号")
    private String phonenumber;
  }

  public static class DictTypeRow {
    @ExcelDictFormat(dictType = "sys_normal_disable")
    @ExcelProperty("状态")
    private String status;
  }
}
