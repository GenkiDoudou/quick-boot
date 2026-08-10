package io.github.genkidoudou.system.internal.online.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 在线用户查询条件。
 */
@Data
@Schema(description = "在线用户列表查询")
public class SysUserOnlineQueryBo {

    @Min(1)
    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Min(1)
    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "登录地址(IP)，模糊匹配")
    private String ipaddr;

    @Schema(description = "用户名，模糊匹配")
    private String userName;
}
