package io.github.genkidoudou.common.exception;

import io.github.genkidoudou.common.api.HttpCodes;

/**
 * 异常体系业务码常量。
 * <p>
 * 分段规则：1xxxx（通用）/ 2xxxx（业务）/ 3xxxx（安全）/ 4xxxx（系统）。
 * 其中部分安全码直接复用既有 {@link HttpCodes} 以避免冲突与双份定义。
 */
public final class ErrorCodes {

  private ErrorCodes() {
  }

  /**
   * 通用错误码。
   */
  public static final class Common {
    /** 参数非法（占位 {0} 为字段或说明）。 */
    public static final int INVALID_PARAM = 10001;
  /** 请求体格式错误或无法解析。 */
  public static final int REQUEST_BODY_INVALID = 10002;
  /** 缺少或非法的幂等键 Header。 */
  public static final int IDEMPOTENCY_KEY_REQUIRED = 10003;
  /** 重复请求（幂等键仍被占用）。 */
  public static final int DUPLICATE_REQUEST = 10004;

    private Common() {
    }
  }


  /**
   * 认证 / 登录安全（3xxxx）。
   */
  public static final class Auth {
    /**
     * 验证码服务 Bean 不可用
     */
    public static final int CAPTCHA_SERVICE_UNAVAILABLE = 30010;
    /**
     * 未装配二次校验
     */
    public static final int CAPTCHA_SECONDARY_NOT_CONFIGURED = 30011;
    /**
     * 未完成行为验证码
     */
    public static final int CAPTCHA_REQUIRED = 30012;
    /**
     * 验证码失效或错误
     */
    public static final int CAPTCHA_INVALID = 30013;
    /**
     * 账号临时锁定（带剩余秒数占位 {0}）
     */
    public static final int ACCOUNT_LOCKED = 30014;
    /**
     * 账号临时锁定（无剩余秒数）
     */
    public static final int ACCOUNT_LOCKED_GENERIC = 30015;
    /**
     * 用户名或密码错误
     */
    public static final int CREDENTIALS_INVALID = 30016;
    /**
     * 账号停用
     */
    public static final int ACCOUNT_DISABLED = 30017;
    /**
     * 失败次数过多已锁定
     */
    public static final int ACCOUNT_LOCKED_BY_RETRY = 30018;

    private Auth() {
    }
  }

  /**
   * 系统错误码。
   */
  public static final class System {
    /** 系统内部错误。 */
    public static final int INTERNAL_ERROR = 40000;
    /** 外部依赖不可用。 */
    public static final int DEPENDENCY_UNAVAILABLE = 40001;

    private System() {
    }
  }

  /**
   * 菜单管理错误码（2xxxx 段）。
   */
  public static final class Menu {
    /** 参数非法（占位 {0}） */
    public static final int INVALID_PARAM = 20040;
    /** 菜单不存在（占位 {0}=menuId） */
    public static final int NOT_FOUND = 20041;
    /** 存在子菜单，不允许删除 */
    public static final int HAS_CHILDREN = 20042;
    /** 上级菜单不能是自己 */
    public static final int PARENT_SELF = 20043;
    /** menuType 非法 */
    public static final int TYPE_INVALID = 20044;

    private Menu() {
    }
  }

  /**
   * 角色管理错误码（2xxxx 段）。
   */
  public static final class Role {
    /** 参数非法（占位 {0} 为字段或说明） */
    public static final int INVALID_PARAM = 20030;
    /** 角色不存在（占位 {0}=roleId） */
    public static final int NOT_FOUND = 20031;
    /** 权限字符已存在（占位 {0}=roleKey） */
    public static final int ROLE_KEY_EXISTS = 20032;
    /** 不允许删除超级管理员角色 */
    public static final int SUPER_ROLE_FORBIDDEN = 20033;
    /** 角色已分配用户，无法删除（占位 {0}=roleId） */
    public static final int HAS_USERS = 20034;
    /** status 仅支持 0/1 */
    public static final int STATUS_INVALID = 20035;

    private Role() {
    }
  }

  /**
   * 定时任务错误码（2xxxx 段）。
   */
  public static final class Job {
    /** Cron 表达式非法。 */
    public static final int CRON_INVALID = 20020;
    /** 调用目标 Bean 不存在。 */
    public static final int INVOKE_TARGET_NOT_FOUND = 20021;
    /** 调用目标不是可执行任务 Bean。 */
    public static final int INVOKE_TARGET_NOT_TASK = 20022;
    /** 任务未注册到调度器。 */
    public static final int JOB_NOT_IN_SCHEDULER = 20023;
    /** 任务类型非法或未启用。 */
    public static final int JOB_TYPE_INVALID = 20024;
    /** HTTP 任务未启用。 */
    public static final int HTTP_JOB_DISABLED = 20025;
    /** HTTP 目标被 SSRF 策略拦截。 */
    public static final int HTTP_JOB_SSRF_BLOCKED = 20026;
    /** 脚本任务未启用。 */
    public static final int SCRIPT_JOB_DISABLED = 20027;
    /** 脚本路径不在白名单。 */
    public static final int SCRIPT_JOB_PATH_DENIED = 20028;
    /** 任务参数 JSON 非法或字段缺失。 */
    public static final int JOB_PARAMS_INVALID = 20029;

    private Job() {
    }
  }

  /**
   * 代码生成错误码（2xxxx 段）。
   */
  public static final class Gen {
    /** 表已导入，不可重复。 */
    public static final int TABLE_ALREADY_IMPORTED = 20010;
    /** 表不存在。 */
    public static final int TABLE_NOT_FOUND = 20011;
    /** SQL 语句非法。 */
    public static final int SQL_INVALID = 20012;
    /** 导入表列表为空。 */
    public static final int IMPORT_TABLES_EMPTY = 20013;
    /** 树形模板暂不支持。 */
    public static final int TREE_TEMPLATE_NOT_SUPPORTED = 20014;
    /** 自定义路径暂不支持。 */
    public static final int CUSTOM_PATH_NOT_SUPPORTED = 20015;

    private Gen() {
    }
  }
}
