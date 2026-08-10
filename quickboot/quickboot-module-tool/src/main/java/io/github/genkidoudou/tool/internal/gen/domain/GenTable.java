package io.github.genkidoudou.tool.internal.gen.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 代码生成业务表。
 */
@Data
@TableName("gen_table")
public class GenTable implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "table_id", type = IdType.ASSIGN_ID)
    private Long tableId;

    private String tableName;
    private String tableComment;
    private String subTableName;
    private String subTableFkName;
    private String className;
    private String tplCategory;
    private String tplWebType;
    private String packageName;
    private String moduleName;
    private String businessName;
    private String functionName;
    private String functionAuthor;
    /** 0 Zip 1 自定义路径 */
    private String genType;
    private String genPath;
    private Long parentMenuId;
    private String treeCode;
    private String treeParentCode;
    private String treeName;
    private String options;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
