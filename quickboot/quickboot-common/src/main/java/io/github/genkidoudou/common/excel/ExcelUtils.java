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
 * Excel 导入导出工具：封装 EasyExcel 读写、字典/大数转换、模板填充与 HTTP 下载响应头。
 */
@UtilityClass
public class ExcelUtils {

  /*******************导出******************************/

  /**
   * 导出 Excel 到 HTTP 响应（无合并、无模板约束）。
   *
   * @param list      数据行
   * @param sheetName sheet 名兼下载文件名前缀
   * @param clazz     行模型
   * @param response  HTTP 响应
   * @param <T>       行类型
   */
  public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, HttpServletResponse response) {
    exportExcel(list, sheetName, clazz, false, false, response);
  }

  /**
   * 导出 Excel，可选启用 {@link CellMergeStrategy} 单元格合并。
   *
   * @param list      数据行
   * @param sheetName sheet 名
   * @param clazz     行模型
   * @param merge     是否合并
   * @param response  HTTP 响应
   * @param <T>       行类型
   */
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

  /**
   * 按 classpath 模板填充数据并导出；数据为空时抛出 {@link ExcelException}。
   *
   * @param data         填充数据（至少一条）
   * @param filename     下载文件名前缀
   * @param templatePath classpath 模板路径
   * @param response     HTTP 响应
   */
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

  /**
   * 生成带 {@code .xlsx} 后缀的安全文件名。
   *
   * @param filename 文件名前缀，空白时默认为 {@code export}
   * @return 完整文件名
   */
  public static String encodingFilename(String filename) {
    String safeName = StringUtils.defaultIfBlank(filename, "export");
    return safeName + ".xlsx";
  }

  /**
   * 设置附件下载响应头（含 CORS 暴露头 {@code download-filename}）。
   *
   * @param response    HTTP 响应
   * @param realFileName 原始文件名
   * @throws UnsupportedEncodingException URL 编码失败
   */
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

  /**
   * RFC 5987 风格百分号编码（空格编码为 {@code %20}）。
   *
   * @param s 待编码字符串
   * @return 编码结果
   * @throws UnsupportedEncodingException 编码失败
   */
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



  /**
   * 监听器模式导入：支持校验、分批回调与行/批消费。
   *
   * @param is           输入流
   * @param clazz        行模型
   * @param isValidate   是否 Bean Validation 校验
   * @param batchSize    批大小，{@code null} 表示不分批
   * @param lineConsumer 行级回调，可为 {@code null}
   * @param listConsumer 批级回调，可为 {@code null}
   * @param <T>          行类型
   * @return 导入统计与错误明细
   */
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

  /**
   * 监听器模式导入（默认开启校验、不分批）。
   *
   * @param is           输入流
   * @param clazz        行模型
   * @param lineConsumer 行级回调
   * @param listConsumer 批级回调
   * @param <T>          行类型
   * @return 导入统计与错误明细
   */
  public static <T> ExcelResult<T> importExcel(InputStream is,
                                               Class<T> clazz,
                                               BiConsumer<T, AnalysisContext> lineConsumer,
                                               BiConsumer<List<T>, AnalysisContext> listConsumer) {
    return importExcel(is, clazz, true, null, lineConsumer, listConsumer);
  }

  /**
   * 使用自定义 {@link ExcelListener} 导入；读取完成后返回监听器内的结果对象。
   *
   * @param is       输入流
   * @param clazz    行模型
   * @param listener 监听器
   * @param <T>      行类型
   * @return 监听器累积的导入结果
   */
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
