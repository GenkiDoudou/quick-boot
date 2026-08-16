package io.github.genkidoudou.common.exception;

import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.i18n.I18nUtil;

/**
 * 统一异常基类，携带业务错误码、默认文案与国际化占位参数。
 * <p>
 * 约定：{@code code} 为空时会兜底为 {@link HttpCodes#INTERNAL_ERROR}，用于保证响应链路始终有可用错误码。
 */
public class BaseException extends RuntimeException {

  private final Integer code;
  private final String msg;
  private final Object[] args;


  /**
   * 仅指定业务码；文案由 {@link I18nUtil#getMessage(Integer)} 解析。
   *
   * @param code 业务错误码，可为空
   */
  public BaseException(Integer code) {
    this(code, null);
  }


  /**
   * @param code 业务错误码；允许为空，为空时兜底为 {@link HttpCodes#INTERNAL_ERROR}
   * @param args 国际化占位参数
   */
  public BaseException(Integer code, Object... args) {

    this(code, I18nUtil.getMessage(code, args), args);
  }

  /**
   * 指定业务码、明确文案与占位参数（跳过仅依赖 code 的 i18n 解析路径）。
   *
   * @param code 业务错误码，可为空
   * @param msg  提示文案
   * @param args 国际化占位参数
   */
  public BaseException(Integer code, String msg, Object[] args) {
    super(msg);
    this.code = code != null ? code : HttpCodes.INTERNAL_ERROR;
    this.msg = msg;
    this.args = args;
  }

  /**
   * @return 业务错误码（永不为空）
   */
  public Integer getCode() {
    return code;
  }

  /**
   * @return 默认文案（允许为空）
   */
  public String getMsg() {
    return msg;
  }

  /**
   * @return 国际化占位参数（允许为空）
   */
  public Object[] getArgs() {
    return args;
  }
}
