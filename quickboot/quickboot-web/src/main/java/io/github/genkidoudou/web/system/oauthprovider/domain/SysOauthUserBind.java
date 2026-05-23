package io.github.genkidoudou.web.system.oauthprovider.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 外部 IdP 与本地用户绑定，表 {@code sys_oauth_user_bind}。
 */
@Data
@TableName("sys_oauth_user_bind")
public class SysOauthUserBind implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String providerCode;

    private String externalSubject;

    private Long userId;

    private LocalDateTime bindTime;

    @TableLogic
    private String delFlag;
}
