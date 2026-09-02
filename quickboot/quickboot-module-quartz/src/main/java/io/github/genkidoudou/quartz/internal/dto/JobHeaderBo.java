package io.github.genkidoudou.quartz.internal.dto;

import lombok.Data;

/**
 * HTTP 请求头键值对（结构化表单一行）。
 */
@Data
public class JobHeaderBo {

    /** 请求头名称。 */
    private String key;

    /** 请求头值。 */
    private String value;
}
