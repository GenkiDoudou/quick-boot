package io.github.genkidoudou.common.api;

/**
 * {@link PageRequest} 与遗留 {@code pageNum/pageSize} 查询 BO 的字段映射辅助。
 */
public final class PageRequestMapping {

  private PageRequestMapping() {
  }

  /**
   * 取页码：优先 {@link PageRequest#getCurrent()}，缺省 1。
   *
   * @param request 分页请求，可为 null
   * @return 页码，最小 1
   */
  public static int pageNum(PageRequest<?> request) {
    return request == null ? 1 : Math.max(1, request.getCurrent());
  }

  /**
   * 取页大小：优先 {@link PageRequest#getSize()}，缺省 10。
   *
   * @param request 分页请求，可为 null
   * @return 页大小，最小 1
   */
  public static int pageSize(PageRequest<?> request) {
    return request == null ? 10 : Math.max(1, request.getSize());
  }
}
