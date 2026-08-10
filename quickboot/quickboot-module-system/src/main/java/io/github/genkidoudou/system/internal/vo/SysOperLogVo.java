package io.github.genkidoudou.system.internal.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志分页 / 详情 / 导出模型（含查询条件字段）。
 */
@Data
@ExcelIgnoreUnannotated
public class SysOperLogVo {

  @ExcelProperty("日志编号")
  private Long operId;

  @ExcelProperty("系统模块")
  private String title;

  @ExcelProperty("业务类型")
  private Integer businessType;

  @ExcelProperty("方法")
  private String method;

  @ExcelProperty("请求方式")
  private String requestMethod;

  private Integer operatorType;

  @ExcelProperty("操作人员")
  private String operName;

  private String deptName;

  @ExcelProperty("请求地址")
  private String operUrl;

  @ExcelProperty("IP")
  private String operIp;

  private String operLocation;

  private String operParam;

  private String jsonResult;

  /** 0 正常，1 异常。 */
  @ExcelProperty("状态")
  private Integer status;

  private String errorMsg;

  @ExcelProperty("操作时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime operTime;

  @ExcelProperty("耗时(ms)")
  private Long costTime;

  @ExcelProperty("链路ID")
  private String traceId;

  @ExcelProperty("操作ID")
  private String clientOperationId;

  @ExcelProperty("客户端ID")
  private String clientId;

  @ExcelProperty("User-Agent")
  private String userAgent;

  /** 勾选导出 / 批量删除主键。 */
  private List<Long> ids;

  /** 操作时间起（yyyy-MM-dd）。 */
  private String beginTime;

  /** 操作时间止（yyyy-MM-dd）。 */
  private String endTime;

  /** 耗时下限（含），毫秒。 */
  private Long costTimeMin;

  /** 耗时上限（含），毫秒。 */
  private Long costTimeMax;
}
