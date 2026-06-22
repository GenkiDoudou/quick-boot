package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MCP 服务展示对象。
 */
@Data
@Schema(description = "MCP 服务")
public class KbMcpServerVo {

    @Schema(description = "MCP ID")
    private Long mcpId;

    @Schema(description = "展示名称")
    private String name;

    @Schema(description = "唯一编码")
    private String code;

    @Schema(description = "备注说明")
    private String description;

    @Schema(description = "传输方式")
    private String transport;

    @Schema(description = "STDIO 命令")
    private String command;

    @Schema(description = "STDIO 参数列表")
    private List<String> args = new ArrayList<>();

    @Schema(description = "远程 MCP URL")
    private String url;

    @Schema(description = "远程 HTTP 请求头")
    private List<McpHeaderItemBo> headers = new ArrayList<>();

    @Schema(description = "请求超时毫秒")
    private Integer requestTimeoutMs;

    @Schema(description = "状态：0 正常 / 1 停用")
    private Integer status;

    @Schema(description = "最近探测状态")
    private String lastTestStatus;

    @Schema(description = "最近探测摘要")
    private String lastTestMsg;

    @Schema(description = "最近探测时间")
    private LocalDateTime lastTestTime;

    @Schema(description = "最近成功测试发现的工具数量")
    private Integer toolCount;

    @Schema(description = "环境变量列表")
    private List<KbMcpEnvVo> envs = new ArrayList<>();

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
