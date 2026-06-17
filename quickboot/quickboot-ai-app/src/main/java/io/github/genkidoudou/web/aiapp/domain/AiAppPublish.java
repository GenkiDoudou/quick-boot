package io.github.genkidoudou.web.aiapp.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 应用发布与嵌入配置实体，对应表 {@code ai_app_publish}。
 */
@Data
@TableName("ai_app_publish")
public class AiAppPublish implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联应用 {@code ai_app.id}。 */
    private Long appId;

    /** 嵌入访问令牌（唯一）。 */
    private String embedToken;

    /** 域名白名单，逗号分隔。 */
    private String allowedOrigins;

    /** 可选系统菜单路由。 */
    private String menuPath;

    /** 可选前端组件路径。 */
    private String menuComponent;

    /** 是否启用嵌入：0 否 / 1 是。 */
    private Integer enabled;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
