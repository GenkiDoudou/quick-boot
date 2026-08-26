package io.github.genkidoudou.system.internal.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志分页 / 导出模型（含查询条件字段）。
 */
@Data
@ExcelIgnoreUnannotated
public class SysLogininforVo {

  /** 访问日志主键。 */
  @ExcelDictFormat(dictType = "sys_login_status")
  @ExcelProperty("访问编号")
  private Long infoId;

  /** 用户主键。 */
  private Long userId;

  /** 登录账号。 */
  @ExcelProperty("用户名称")
  private String userName;

  /** OAuth 客户端 ID。 */
  @ExcelProperty("客户端ID")
  private String clientId;

  /** 登录 IP。 */
  @ExcelProperty("登录地址")
  private String ipaddr;

  /** 登录地点。 */
  @ExcelProperty("登录地点")
  private String loginLocation;

  /** 浏览器类型。 */
  @ExcelProperty("浏览器")
  private String browser;

  /** 操作系统。 */
  @ExcelProperty("操作系统")
  private String os;

  /** 0 成功，1 失败。 */
  @ExcelProperty("登录状态")
  private String status;

  /** 提示消息。 */
  @ExcelProperty("提示消息")
  private String msg;

  /** 登录时间。 */
  @ExcelProperty("登录时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime loginTime;

  /** 勾选导出 / 批量删除主键。 */
  private List<Long> ids;

  /** 登录时间起（yyyy-MM-dd）。 */
  private String beginTime;

  /** 登录时间止（yyyy-MM-dd）。 */
  private String endTime;
}
