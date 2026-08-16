package io.github.genkidoudou.common.exception;

/**
 * 可预期异常：用于业务校验失败、安全拦截等可被调用方理解和处理的场景。
 * <p>
 * 约定：
 * <ul>
 *   <li>{@link #WarningException(Integer, Object...)} — 第二参及之后为 i18n 占位参数；</li>
 *   <li>{@link #literal(Integer, String)} — 字面文案，跳过仅依赖 code 的 i18n 解析。</li>
 * </ul>
 * 禁止再提供 {@code (Integer, String)} 构造，以免与占位参数重载冲突。
 */
public class WarningException extends BaseException {

  /**
   * 仅指定业务码。
   *
   * @param code 业务错误码
   */
  public WarningException(Integer code) {
    super(code);
  }

  /**
   * 指定业务码与 i18n 占位参数。
   *
   * @param code 业务错误码
   * @param args 国际化占位参数
   */
  public WarningException(Integer code, Object... args) {
    super(code, args);
  }

  /**
   * 指定业务码与明确字面文案（不把 {@code message} 当作 i18n 占位参数）。
   *
   * @param code    业务错误码
   * @param message 字面提示文案
   * @return 异常实例
   */
  public static WarningException literal(Integer code, String message) {
    return new WarningException(code, message, null);
  }

  private WarningException(Integer code, String msg, Object[] args) {
    super(code, msg, args);
  }
}
