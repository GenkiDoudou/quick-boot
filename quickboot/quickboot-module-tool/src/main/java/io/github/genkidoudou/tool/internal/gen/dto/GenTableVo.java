package io.github.genkidoudou.tool.internal.gen.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代码生成表详情/列表 VO。
 */
@Data
public class GenTableVo {

    private Long tableId;
    private String tableName;
    private String tableComment;
    private String className;
    private String tplCategory;
    /** 前端模板：c7 / element-plus */
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
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
