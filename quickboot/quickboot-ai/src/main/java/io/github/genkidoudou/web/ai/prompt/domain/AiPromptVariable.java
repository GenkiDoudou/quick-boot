package io.github.genkidoudou.web.ai.prompt.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 提示词变量声明实体，对应 {@code ai_prompt_variable}。
 */
@Data
@TableName("ai_prompt_variable")
public class AiPromptVariable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "variable_id", type = IdType.ASSIGN_ID)
    private Long variableId;

    /** 所属提示词 ID。 */
    private Long promptId;

    /** 版本 ID；0 为草稿。 */
    private Long versionId;

    /** 变量键名。 */
    private String varKey;

    /**
     * 变量类型：string / number / array / object。
     *
     * @see io.github.genkidoudou.web.ai.prompt.constants.AiPromptVarType
     */
    private String varType;

    /** 是否必填：0 否 / 1 是。 */
    private Integer required;

    /** 变量说明。 */
    private String description;

    /** 排序号。 */
    private Integer sort;
}
