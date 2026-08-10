package io.github.genkidoudou.common.excel.dict;

/**
 * Excel 字典未匹配策略。
 */
public enum DictMissPolicy {
  /** 保留原文。 */
  KEEP,
  /** 该项置空（多值时去掉空段）。 */
  EMPTY,
  /** 抛出 Excel 校验异常。 */
  ERROR
}
