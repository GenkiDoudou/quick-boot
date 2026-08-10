package io.github.genkidoudou.common.monitor.operlog;

/**
 * 操作日志业务类型，与字典 {@code sys_oper_business_type} 数值一致。
 */
public final class OperLogBusinessType {

  /** 其它。 */
  public static final int OTHER = 0;
  /** 新增。 */
  public static final int INSERT = 1;
  /** 修改。 */
  public static final int UPDATE = 2;
  /** 删除。 */
  public static final int DELETE = 3;
  /** 导出。 */
  public static final int EXPORT = 4;
  /** 导入。 */
  public static final int IMPORT = 5;

  private OperLogBusinessType() {
  }
}
