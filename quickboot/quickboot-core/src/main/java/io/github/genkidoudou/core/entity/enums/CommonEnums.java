package io.github.genkidoudou.core.entity.enums;

import lombok.Getter;

/**
 * 通用枚举
 *
 * @author luyanan
 * @since 2026/7/27
 */
@Getter
public enum CommonEnums {

  // 正常
  STATUS_ENABLE(IEnumType.STATUS, "0", "正常"),
  // 禁用

  STATUS_DISABLE(IEnumType.STATUS, "1", "禁用"),
  ;


  CommonEnums(IEnumType enumType, String value, String label) {
    this.enumType = enumType;
    this.value = value;
    this.label = label;
  }

  private IEnumType enumType;

  private String value;

  private String label;


  public enum IEnumType {

    // 状态
    STATUS,

  }
}
