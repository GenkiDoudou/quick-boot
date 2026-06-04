package io.github.genkidoudou.web.system.dict.type.dto;

import lombok.Data;

/**
 * 字典类型列表/导出筛选条件（与列表查询参数一致）。
 */
@Data
public class SysDictTypeQueryBo {

    private String dictName;
    private String dictType;
    private String status;
}
