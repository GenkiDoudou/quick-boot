package io.github.genkidoudou.common.excel.dict;

import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import io.github.genkidoudou.common.excel.exception.ExcelDataCheckException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 字典转换引擎与内联解析最小验证。
 */
class DictConvertEngineTest {

  @AfterEach
  void tearDown() {
    DictLookupHolder.clear();
  }

  @Test
  void parseDictText_firstEqualsOnly() {
    Map<String, String> map = DictTextParser.parseValueToLabel(new String[]{"0=男=备用", "bad"});
    assertEquals("男=备用", map.get("0"));
    assertFalse(map.containsKey("bad"));
  }

  @Test
  void inline_singleValue_roundTrip() {
    ExcelDictFormat format = format("", new String[]{"0=男", "1=女"}, ",", DictMissPolicy.KEEP);
    assertEquals("男", DictConvertEngine.toLabels("0", format, "sex"));
    assertEquals("0", DictConvertEngine.toValues("男", format, "sex"));
  }

  @Test
  void inline_multiValue_roundTrip() {
    ExcelDictFormat format = format("", new String[]{"0=男", "1=女"}, ",", DictMissPolicy.KEEP);
    assertEquals("男,女", DictConvertEngine.toLabels("0,1", format, "sex"));
    assertEquals("0,1", DictConvertEngine.toValues("男,女", format, "sex"));
  }

  @Test
  void inline_importAcceptsRawValue() {
    ExcelDictFormat format = format("", new String[]{"0=男"}, ",", DictMissPolicy.KEEP);
    assertEquals("0", DictConvertEngine.toValues("0", format, "sex"));
  }

  @Test
  void missPolicy_keep() {
    ExcelDictFormat format = format("", new String[]{"0=男"}, ",", DictMissPolicy.KEEP);
    assertEquals("未知", DictConvertEngine.toLabels("未知", format, "sex"));
  }

  @Test
  void missPolicy_empty_multiValue() {
    ExcelDictFormat format = format("", new String[]{"0=男"}, ",", DictMissPolicy.EMPTY);
    assertEquals("男", DictConvertEngine.toLabels("0,未知", format, "sex"));
  }

  @Test
  void missPolicy_error() {
    ExcelDictFormat format = format("", new String[]{"0=男"}, ",", DictMissPolicy.ERROR);
    assertThrows(ExcelDataCheckException.class,
      () -> DictConvertEngine.toLabels("未知", format, "sex"));
  }

  @Test
  void blank_passthrough() {
    ExcelDictFormat format = format("", new String[]{"0=男"}, ",", DictMissPolicy.KEEP);
    assertNull(DictConvertEngine.toLabels(null, format, "sex"));
    assertEquals("", DictConvertEngine.toLabels("", format, "sex"));
  }

  @Test
  void lookupMissing_error() {
    ExcelDictFormat format = format("sys_user_sex", new String[]{}, ",", DictMissPolicy.ERROR);
    ExcelDataCheckException ex = assertThrows(ExcelDataCheckException.class,
      () -> DictConvertEngine.toLabels("0", format, "sex"));
    assertTrue(ex.getMessage().contains("字典服务未就绪"));
  }

  @Test
  void lookupMissing_keep() {
    ExcelDictFormat format = format("sys_user_sex", new String[]{}, ",", DictMissPolicy.KEEP);
    assertEquals("0", DictConvertEngine.toLabels("0", format, "sex"));
  }

  @Test
  void dictType_usesLookup() {
    DictLookupHolder.set(new DictLookup() {
      @Override
      public String getLabel(String dictType, String value) {
        return "0".equals(value) ? "男" : null;
      }

      @Override
      public String getValue(String dictType, String label) {
        return "男".equals(label) ? "0" : null;
      }
    });
    ExcelDictFormat format = format("sys_user_sex", new String[]{"9=忽略"}, ",", DictMissPolicy.KEEP);
    assertEquals("男", DictConvertEngine.toLabels("0", format, "sex"));
    assertEquals("0", DictConvertEngine.toValues("男", format, "sex"));
  }

  private static ExcelDictFormat format(String dictType,
                                        String[] dictText,
                                        String separator,
                                        DictMissPolicy missPolicy) {
    InvocationHandler handler = (proxy, method, args) -> dispatch(method, dictType, dictText, separator, missPolicy);
    return (ExcelDictFormat) Proxy.newProxyInstance(
      ExcelDictFormat.class.getClassLoader(),
      new Class<?>[]{ExcelDictFormat.class},
      handler);
  }

  private static Object dispatch(Method method,
                                 String dictType,
                                 String[] dictText,
                                 String separator,
                                 DictMissPolicy missPolicy) {
    return switch (method.getName()) {
      case "dictType" -> dictType;
      case "dictText" -> dictText;
      case "separator" -> separator;
      case "missPolicy" -> missPolicy;
      case "annotationType" -> ExcelDictFormat.class;
      case "equals" -> false;
      case "hashCode" -> System.identityHashCode(method);
      case "toString" -> "@ExcelDictFormat(proxy)";
      default -> {
        if (method.getDeclaringClass() == Annotation.class || method.getDeclaringClass() == Object.class) {
          yield null;
        }
        throw new UnsupportedOperationException(method.getName());
      }
    };
  }
}
