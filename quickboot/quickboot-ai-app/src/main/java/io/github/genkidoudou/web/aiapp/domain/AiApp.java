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
 * AI 应用定义实体，对应表 {@code ai_app}。
 */
@Data
@TableName("ai_app")
public class AiApp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 应用名称。 */
    private String name;

    /** 功能介绍。 */
    private String description;

    /** 图标 URL 或内置 key。 */
    private String icon;

    /**
     * 应用类型：agent / workflow。
     *
     * @see io.github.genkidoudou.web.aiapp.constants.AiAppType
     */
    private String appType;

    /**
     * 状态：draft / published。
     *
     * @see io.github.genkidoudou.web.aiapp.constants.AiAppStatus
     */
    private String status;

    /** 草稿配置 JSON。 */
    private String configJson;

    /** 发布快照 JSON。 */
    private String publishedConfigJson;

    /** 逻辑删除：0 否 / 1 是。 */
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
