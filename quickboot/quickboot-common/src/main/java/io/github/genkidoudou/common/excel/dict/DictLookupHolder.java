package io.github.genkidoudou.common.excel.dict;

/**
 * {@link DictLookup} 静态挂载点，供静态 {@code ExcelUtils} / Converter 取实现。
 */
public final class DictLookupHolder {

  private static volatile DictLookup lookup;

  private DictLookupHolder() {
  }

  /**
   * 注册系统字典实现（通常由 AutoConfiguration 调用）。
   *
   * @param dictLookup 实现；可为 {@code null} 表示清空
   */
  public static void set(DictLookup dictLookup) {
    lookup = dictLookup;
  }

  /**
   * @return 当前实现，未注册时为 {@code null}
   */
  public static DictLookup get() {
    return lookup;
  }

  /**
   * 清空挂载（测试用）。
   */
  public static void clear() {
    lookup = null;
  }
}
