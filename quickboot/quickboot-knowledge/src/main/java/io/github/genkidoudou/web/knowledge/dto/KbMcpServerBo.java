package io.github.genkidoudou.web.knowledge.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP 服务新增/修改入参。
 */
@Data
@Schema(description = "MCP 服务业务入参")
public class KbMcpServerBo {

    @NotNull(message = "MCP ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "MCP ID（修改必填）")
    private Long mcpId;

    @NotBlank(message = "名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "名称长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "展示名称")
    private String name;

    @NotBlank(message = "编码不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 64, message = "编码长度不能超过64", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "唯一编码")
    private String code;

    @Size(max = 500, message = "描述长度不能超过500", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "备注说明")
    private String description;

    @NotBlank(message = "传输方式不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "传输方式：STDIO / SSE / STREAMABLE_HTTP")
    private String transport;

    @Size(max = 255, message = "命令长度不能超过255", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "STDIO 命令")
    private String command;

    @Schema(description = "STDIO 参数列表")
    private List<String> args = new ArrayList<>();

    @Size(max = 2048, message = "URL 长度不能超过2048", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "远程 MCP URL")
    private String url;

    @Valid
    @Schema(description = "远程 HTTP 请求头")
    private List<McpHeaderItemBo> headers = new ArrayList<>();

    @Min(value = 1000, message = "超时不能小于1000毫秒", groups = {AddGroup.class, UpdateGroup.class})
    @Max(value = 300_000, message = "超时不能大于300000毫秒", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "请求超时毫秒")
    private Integer requestTimeoutMs;

    @Min(value = 0, message = "状态值无效", groups = {AddGroup.class, UpdateGroup.class})
    @Max(value = 1, message = "状态值无效", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "状态：0 正常 / 1 停用")
    private Integer status;

    @Valid
    @Schema(description = "环境变量列表")
    private List<KbMcpEnvBo> envs = new ArrayList<>();
}
