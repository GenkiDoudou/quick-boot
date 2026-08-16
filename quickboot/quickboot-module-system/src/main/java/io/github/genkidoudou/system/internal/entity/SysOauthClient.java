package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.genkidoudou.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * OAuth2 客户端，表 {@code sys_oauth_client}。
 * <p>主键 {@code id}；{@code clientId} 为业务唯一标识（登录 Basic 鉴权用）。</p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_oauth_client")
public class SysOauthClient extends BaseEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /** 管理端主键（雪花） */
  @TableId(value = "id", type = IdType.ASSIGN_ID)
  private Long id;

  /** 客户端业务标识（唯一，创建后不可改） */
  private String clientId;

  /** 客户端密钥（加密或明文存储，依部署策略）。 */
  private String clientSecret;

  /** 客户端展示名称。 */
  private String clientName;

  /**
   * 允许访问的接口 path（Ant 风格，如 /system/**），逗号分隔
   */
  private String apiPathPatterns;

  /**
   * token 有效时间（秒）；空则走全局 sa-token 配置
   */
  private Long tokenTimeout;

  /**
   * 是否校验验证码：{@code 0}=否，{@code 1}=是（禁止 Boolean）
   */
  private String checkCaptcha;

  /** 0=启用 1=停用 */
  private String status;
}
