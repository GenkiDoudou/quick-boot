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

  @ExcelDictFormat(dictType = "sys_login_status")
  @ExcelProperty("访问编号")
  private Long infoId;

  private Long userId;

  @ExcelProperty("用户名称")
  private String userName;

  @ExcelProperty("客户端ID")
  private String clientId;

  @ExcelProperty("登录地址")
  private String ipaddr;

  @ExcelProperty("登录地点")
  private String loginLocation;

  @ExcelProperty("浏览器")
  private String browser;

  @ExcelProperty("操作系统")
  private String os;

  /** 0 成功，1 失败。 */
  @ExcelProperty("登录状态")
  private String status;

  @ExcelProperty("提示消息")
  private String msg;

  @ExcelProperty("登录时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime loginTime;

  /** 勾选导出 / 批量删除主键。 */
  private List<Long> ids;

  /** 登录时间起（yyyy-MM-dd）。 */
  private String beginTime;

  /** 登录时间止（yyyy-MM-dd）。 */
  private String endTime;
}
