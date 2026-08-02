package io.github.genkidoudou.common.api;

/**
 * 与 HTTP 语义对齐的<strong>业务响应码</strong>常量，用于 {@link R} 的 {@code code} 字段。
 * <p>
 * 约定：对外 API 的 HTTP 状态码保持 200，由客户端依据本处整型码判断业务成败（见项目 AGENTS / OpenSpec {@code common-response-paging}）。
 */
public final class HttpCodes {

  /**
   * 成功，与 {@link R#isSuccess()} 判定一致。
   */
  public static final int OK = 200;

  public static final int BAD_REQUEST = 400;
  public static final int UNAUTHORIZED = 401;
  public static final int FORBIDDEN = 403;
  public static final int NOT_FOUND = 404;
  public static final int INTERNAL_ERROR = 500;
  public static final int SERVICE_UNAVAILABLE = 503;


  private HttpCodes() {
  }
}
