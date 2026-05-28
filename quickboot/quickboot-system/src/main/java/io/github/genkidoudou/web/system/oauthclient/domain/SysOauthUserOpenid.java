package io.github.genkidoudou.web.system.oauthclient.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OAuth2 用户 openid 映射，表 {@code sys_oauth_user_openid}。
 */
@Data
@TableName("sys_oauth_user_openid")
public class SysOauthUserOpenid implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String clientId;

    private Long userId;

    private String openid;

    @TableLogic
    private String delFlag;

    private LocalDateTime createTime;
}
