package io.github.genkidoudou.web.tool.gen.dto;

import lombok.Data;

/**
 * 数据库候选表信息。
 */
@Data
public class GenDbTableVo {

    private String tableName;
    private String tableComment;
}
