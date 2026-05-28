package io.github.genkidoudou.web.system.notice.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知公告实体。
 */
@Data
@TableName("sys_notice")
public class SysNotice implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "notice_id", type = IdType.ASSIGN_ID)
    private Long noticeId;

    private String noticeTitle;

    /** 字典 sys_notice_type：1 通知 2 公告 */
    private String noticeType;

    /** 消毒后的 HTML */
    private String noticeContent;

    /** 字典 sys_notice_status：0 正常 1 关闭 */
    private String status;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
