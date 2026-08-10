package io.github.genkidoudou.report.internal.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 积木报表 / BI 大屏目录项，供菜单维护时下拉选择。
 */
@Data
@Schema(description = "积木目录项")
public class JimuCatalogItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键 ID")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "编码（报表可选）")
    private String code;
}
