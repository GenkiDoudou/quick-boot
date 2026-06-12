package io.github.genkidoudou.web.knowledge.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 外部 MCP 服务配置实体，对应表 {@code kb_mcp_server}。
 */
@Data
@TableName("kb_mcp_server")
public class KbMcpServer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "mcp_id", type = IdType.ASSIGN_ID)
    private Long mcpId;

    /** 展示名称。 */
    private String name;

    /** 唯一编码，导出 mcpServers 的 key。 */
    private String code;

    /** 备注说明。 */
    private String description;

    /**
     * 传输方式：STDIO / SSE / STREAMABLE_HTTP。
     *
     * @see io.github.genkidoudou.web.knowledge.constants.McpTransport
     */
    private String transport;

    /** STDIO 可执行命令。 */
    private String command;

    /** STDIO 参数 JSON 数组字符串。 */
    private String argsJson;

    /** 远程 MCP 根 URL。 */
    private String url;

    /** 远程 HTTP 请求头 JSON 字符串。 */
    private String headersJson;

    /** 请求超时（毫秒）。 */
    private Integer requestTimeoutMs;

    /** 状态：0 正常 / 1 停用。 */
    private Integer status;

    /**
     * 最近探测状态：SUCCESS / FAILED / UNTESTED。
     *
     * @see io.github.genkidoudou.web.knowledge.constants.McpTestStatus
     */
    private String lastTestStatus;

    /** 最近探测摘要。 */
    private String lastTestMsg;

    /** 最近探测时间。 */
    private LocalDateTime lastTestTime;

    /** 逻辑删除：0 否 / 1 是。 */
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
