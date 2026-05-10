package io.github.genkidoudou.web.system.dict.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 字典项保存请求。 */
@Data
public class SysDictDataSaveRequest {
    private Long dictCode;
    @NotNull(message = "排序不能为空")
    private Integer dictSort;
    @NotBlank(message = "数据标签不能为空")
    @Size(max = 100, message = "数据标签长度不能超过100")
    private String dictLabel;
    @NotBlank(message = "数据键值不能为空")
    @Size(max = 100, message = "数据键值长度不能超过100")
    private String dictValue;
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型长度不能超过100")
    private String dictType;
    @Size(max = 100, message = "样式长度不能超过100")
    private String cssClass;
    @Size(max = 100, message = "回显样式长度不能超过100")
    private String listClass;
    @Pattern(regexp = "^[01]$", message = "状态必须为0或1")
    private String status;
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
