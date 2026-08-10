package io.github.genkidoudou.common.excel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.WriteDirectionEnum;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import io.github.genkidoudou.common.excel.conver.ExcelBigNumberConvert;
import io.github.genkidoudou.common.excel.conver.ExcelDictConvert;
import io.github.genkidoudou.common.excel.conver.merge.CellMergeStrategy;
import io.github.genkidoudou.common.excel.exception.ExcelException;
import io.github.genkidoudou.common.excel.listener.ExcelListener;
import io.github.genkidoudou.common.excel.listener.ExcelListenerCallback;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.excel.template.TemplateConstraintWriteHandler;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * excel 导出工具类
 *
 * @author luyanan
 * @since 2026/8/2
 */
@UtilityClass
public class ExcelUtils {

  /*******************导出******************************/
  public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, HttpServletResponse response) {
    exportExcel(list, sheetName, clazz, false, false, response);
  }

  public static <T> void exportExcel(List<T> list,
                                     String sheetName,
                                     Class<T> clazz,
                                     boolean merge,
                                     HttpServletResponse response) {
    exportExcel(list, sheetName, clazz, merge, false, response);
  }

  /**
   * 导出 Excel。
   *
   * @param list                     数据；导入模板常为空列表
   * @param sheetName                sheet / 文件名
   * @param clazz                    行模型
   * @param merge                    是否启用单元格合并策略
   * @param applyTemplateConstraints 为 true 时按注解写入导入模板列约束（下拉/输入提示）
   * @param response                 HTTP 响应
   */
  public static <T> void exportExcel(List<T> list,
                                     String sheetName,
                                     Class<T> clazz,
                                     boolean merge,
                                     boolean applyTemplateConstraints,
                                     HttpServletResponse response) {
    try {
      resetResponse(sheetName, response);
      ServletOutputStream os = response.getOutputStream();
      ExcelWriterSheetBuilder builder = registerConverters(EasyExcel.write(os, clazz))
        .autoCloseStream(false)
        .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
        .sheet(sheetName);
      if (merge) {
        builder.registerWriteHandler(new CellMergeStrategy(list, true));
      }
      if (applyTemplateConstraints) {
        builder.registerWriteHandler(new TemplateConstraintWriteHandler(clazz));
      }
      builder.doWrite(list);
    } catch (IOException e) {
      throw new ExcelException("导出 Excel 异常", e);
    }
  }

  public static void exportTemplate(List<Object> data,
                                    String filename,
                                    String templatePath,
                                    HttpServletResponse response) {
    try {
      resetResponse(filename, response);
      ClassPathResource templateResource = new ClassPathResource(templatePath);
      ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
        .withTemplate(templateResource.getStream())
        .autoCloseStream(false)
        .registerConverter(new ExcelBigNumberConvert())
        .registerConverter(new ExcelDictConvert())
        .build();
      WriteSheet writeSheet = EasyExcel.writerSheet().build();
      if (CollUtil.isEmpty(data)) {
        throw new ExcelException("导出数据不能为空");
      }
      FillConfig fillConfig = FillConfig.builder()
        .forceNewRow(Boolean.TRUE)
        .direction(WriteDirectionEnum.VERTICAL)
        .build();
      for (Object d : data) {
        excelWriter.fill(d, fillConfig, writeSheet);
      }
      excelWriter.finish();
    } catch (IOException e) {
      throw new ExcelException("导出 Excel 异常", e);
    }
  }

  private static void resetResponse(String sheetName, HttpServletResponse response) throws UnsupportedEncodingException {
    String filename = encodingFilename(sheetName);
    setAttachmentResponseHeader(response, filename);
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
  }

  public static String encodingFilename(String filename) {
    String safeName = StringUtils.defaultIfBlank(filename, "export");
    return safeName + ".xlsx";
  }

  public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) throws UnsupportedEncodingException {
    String percentEncodedFileName = percentEncode(realFileName);

    String contentDispositionValue = "attachment; filename=" +
      percentEncodedFileName +
      ";" +
      "filename*=" +
      "utf-8''" +
      percentEncodedFileName;

    response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
    response.setHeader("Content-disposition", contentDispositionValue);
    response.setHeader("download-filename", percentEncodedFileName);
  }

  public static String percentEncode(String s) throws UnsupportedEncodingException {
    String encode = URLEncoder.encode(s, StandardCharsets.UTF_8);
    return encode.replaceAll("\\+", "%20");
  }

  /***********************导入*****************/

  /**
   * 同步读取全部行（对齐 bak：EasyExcel doReadSync）。
   *
   * @param is    输入流
   * @param clazz 行模型
   * @return 行列表
   */
  public static <T> List<T> importExcelSync(InputStream is, Class<T> clazz) {
    return registerConverters(EasyExcel.read(is).head(clazz).autoCloseStream(false))
      .sheet()
      .doReadSync();
  }

  /**
   * 与 bak 同名：同步读取全部行。
   *
   * @param is    输入流
   * @param clazz 行模型
   * @return 行列表
   */
  public static <T> List<T> importExcel(InputStream is, Class<T> clazz) {
    return importExcelSync(is, clazz);
  }



  public static <T> ExcelResult<T> importExcel(InputStream is,
                                               Class<T> clazz,
                                               boolean isValidate,
                                               Long batchSize,
                                               BiConsumer<T, AnalysisContext> lineConsumer,
                                               BiConsumer<List<T>, AnalysisContext> listConsumer) {
    return importExcel(is, clazz, new ExcelListenerCallback<T>(isValidate, batchSize) {
      @Override
      protected void callback(T data, AnalysisContext context) {
        if (lineConsumer != null) {
          lineConsumer.accept(data, context);
        }
      }

      @Override
      protected void getList(List<T> list, AnalysisContext context) {
        if (listConsumer != null) {
          listConsumer.accept(list, context);
        }
      }
    });
  }

  public static <T> ExcelResult<T> importExcel(InputStream is,
                                               Class<T> clazz,
                                               BiConsumer<T, AnalysisContext> lineConsumer,
                                               BiConsumer<List<T>, AnalysisContext> listConsumer) {
    return importExcel(is, clazz, true, null, lineConsumer, listConsumer);
  }

  public static <T> ExcelResult<T> importExcel(InputStream is, Class<T> clazz, ExcelListener<T> listener) {
    registerConverters(EasyExcel.read(is, clazz, listener)).sheet().doRead();
    return listener.getExcelResult();
  }

  private static ExcelWriterBuilder registerConverters(ExcelWriterBuilder builder) {
    return builder
      .registerConverter(new ExcelBigNumberConvert())
      .registerConverter(new ExcelDictConvert());
  }

  private static ExcelReaderBuilder registerConverters(ExcelReaderBuilder builder) {
    return builder
      .registerConverter(new ExcelBigNumberConvert())
      .registerConverter(new ExcelDictConvert());
  }

}
