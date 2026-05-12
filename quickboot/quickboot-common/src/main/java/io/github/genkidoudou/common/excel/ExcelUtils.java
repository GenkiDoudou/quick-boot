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
import com.alibaba.excel.write.metadata.fill.FillWrapper;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import io.github.genkidoudou.common.excel.convert.ExcelBigNumberConvert;
import io.github.genkidoudou.common.excel.exception.ExcelException;
import io.github.genkidoudou.common.excel.listener.ExcelListener;
import io.github.genkidoudou.common.excel.listener.ExcelListenerCallback;
import io.github.genkidoudou.common.excel.merge.CellMergeStrategy;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Excel 工具类。
 */
@UtilityClass
public class ExcelUtils {

  /**
   * 下载文件名生成策略。
   */
  public enum FileNameStrategy {
    ORIGINAL,
    TIMESTAMP,
    UUID
  }

  private static volatile FileNameStrategy fileNameStrategy = FileNameStrategy.UUID;

  public static void setFileNameStrategy(FileNameStrategy strategy) {
    if (strategy != null) {
      fileNameStrategy = strategy;
    }
  }

  public static <T> List<T> importExcel(InputStream is, Class<T> clazz) {
    return EasyExcel.read(is).head(clazz).autoCloseStream(false).sheet().doReadSync();
  }

  public static <T> ExcelResult<T> importExcel(InputStream is, Class<T> clazz, ExcelListener<T> listener) {
    EasyExcel.read(is, clazz, listener).sheet().doRead();
    return listener.getExcelResult();
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

  public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, HttpServletResponse response) {
    exportExcel(list, sheetName, clazz, false, response);
  }

  public static <T> void exportExcel(List<T> list,
                                     String sheetName,
                                     Class<T> clazz,
                                     boolean merge,
                                     HttpServletResponse response) {
    try {
      resetResponse(sheetName, response);
      ServletOutputStream os = response.getOutputStream();
      ExcelWriterSheetBuilder builder = EasyExcel.write(os, clazz)
        .autoCloseStream(false)
        .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
        .registerConverter(new ExcelBigNumberConvert())
        .sheet(sheetName);
      if (merge) {
        builder.registerWriteHandler(new CellMergeStrategy(list, true));
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

  public static void exportTemplateMultiList(Map<String, Object> data,
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
        .build();
      WriteSheet writeSheet = EasyExcel.writerSheet().build();
      if (CollUtil.isEmpty(data)) {
        throw new ExcelException("导出数据不能为空");
      }
      for (Map.Entry<String, Object> map : data.entrySet()) {
        FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
        if (map.getValue() instanceof Collection) {
          excelWriter.fill(new FillWrapper(map.getKey(), (Collection<?>) map.getValue()), fillConfig, writeSheet);
        } else {
          excelWriter.fill(map.getValue(), writeSheet);
        }
      }
      excelWriter.finish();
    } catch (IOException e) {
      throw new ExcelException("导出 Excel 异常", e);
    }
  }

  /**
   * 导出到字节数组，常用于失败明细文件回传。
   */
  public static <T> byte[] writeBytes(String sheetName, Class<T> clazz, List<T> rows) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      EasyExcel.write(out, clazz)
        .sheet(sheetName)
        .doWrite(rows);
      return out.toByteArray();
    } catch (IOException e) {
      throw new ExcelException("导出 Excel 字节流异常", e);
    }
  }

  private static void resetResponse(String sheetName, HttpServletResponse response) throws UnsupportedEncodingException {
    String filename = encodingFilename(sheetName);
    setAttachmentResponseHeader(response, filename);
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
  }

  public static String percentEncode(String s) throws UnsupportedEncodingException {
    String encode = URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
    return encode.replaceAll("\\+", "%20");
  }

  public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) throws UnsupportedEncodingException {
    String percentEncodedFileName = percentEncode(realFileName);

    StringBuilder contentDispositionValue = new StringBuilder();
    contentDispositionValue.append("attachment; filename=")
      .append(percentEncodedFileName)
      .append(";")
      .append("filename*=")
      .append("utf-8''")
      .append(percentEncodedFileName);

    response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
    response.setHeader("Content-disposition", contentDispositionValue.toString());
    response.setHeader("download-filename", percentEncodedFileName);
  }

  public static String convertByExp(String propertyValue, String converterExp, String separator) {
    StringBuilder propertyString = new StringBuilder();
    String[] convertSource = converterExp.split(",");
    for (String item : convertSource) {
      String[] itemArray = item.split("=");
      if (StringUtils.containsAny(propertyValue, separator)) {
        for (String value : propertyValue.split(separator)) {
          if (itemArray[0].equals(value)) {
            propertyString.append(itemArray[1]).append(separator);
            break;
          }
        }
      } else if (itemArray[0].equals(propertyValue)) {
        return itemArray[1];
      }
    }
    return StringUtils.stripEnd(propertyString.toString(), separator);
  }

  public static String reverseByExp(String propertyValue, String converterExp, String separator) {
    StringBuilder propertyString = new StringBuilder();
    String[] convertSource = converterExp.split(",");
    for (String item : convertSource) {
      String[] itemArray = item.split("=");
      if (StringUtils.containsAny(propertyValue, separator)) {
        for (String value : propertyValue.split(separator)) {
          if (itemArray[1].equals(value)) {
            propertyString.append(itemArray[0]).append(separator);
            break;
          }
        }
      } else if (itemArray[1].equals(propertyValue)) {
        return itemArray[0];
      }
    }
    return StringUtils.stripEnd(propertyString.toString(), separator);
  }

  public static String encodingFilename(String filename) {
    String safeName = StringUtils.defaultIfBlank(filename, "export");
    return switch (fileNameStrategy) {
      case ORIGINAL -> safeName + ".xlsx";
      case TIMESTAMP -> System.currentTimeMillis() + "_" + safeName + ".xlsx";
      case UUID -> IdUtil.fastSimpleUUID() + "_" + safeName + ".xlsx";
    };
  }
}
