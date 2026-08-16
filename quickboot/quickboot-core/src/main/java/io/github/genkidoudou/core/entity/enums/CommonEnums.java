package io.github.genkidoudou.core.entity.enums;

import lombok.Getter;

/**
 * 通用业务枚举：按 {@link IEnumType} 分组，提供字典取值与展示标签。
 *
 * @author luyanan
 * @since 2026/7/27
 */
@Getter
public enum CommonEnums {

  /** 状态：正常（{@code 0}）。 */
  STATUS_ENABLE(IEnumType.STATUS, "0", "正常"),
  /** 状态：禁用（{@code 1}）。 */
  STATUS_DISABLE(IEnumType.STATUS, "1", "禁用"),
  ;

  /**
   * @param enumType 枚举分组
   * @param value    字典取值
   * @param label    展示标签
   */
  CommonEnums(IEnumType enumType, String value, String label) {
    this.enumType = enumType;
    this.value = value;
    this.label = label;
  }

  /** 枚举分组。 */
  private IEnumType enumType;

  /** 字典取值（落库 / 接口传输）。 */
  private String value;

  /** 展示标签。 */
  private String label;


  /** 枚举分组类型。 */
  public enum IEnumType {

    /** 通用状态（正常 / 禁用）。 */
    STATUS,

  }
}
