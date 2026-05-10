package io.github.genkidoudou.web.system.dict.type.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 字典类型保存请求。 */
@Data
@Schema(description = "字典类型保存请求")
public class SysDictTypeSaveRequest {
    @Schema(description = "字典ID")
    private Long dictId;
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称长度不能超过100")
    private String dictName;
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型长度不能超过100")
    private String dictType;
    @Pattern(regexp = "^[01]$", message = "状态必须为0或1")
    private String status;
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
