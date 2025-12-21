package com.su60.quickboot.system.dos;

import cn.t200.quickboot.common.validation.AddGroup;
import cn.t200.quickboot.common.validation.UpdateGroup;
import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.Date;

/**
* <p>
* 部门表
* </p>
* @author luyanan
* @since 2025/11/27
*/
@Data
@Accessors(chain = true)
public class SysDeptDo implements Serializable{
@Serial
private static final long serialVersionUID = 1L;



    /**
    * 部门id
    * @since 2025/11/27
    */
    @NotNull(groups = UpdateGroup.class, message = "部门id 不能为空")
    private Long deptId;




    /**
    * 父部门id
    * @since 2025/11/27
    */
    private Long parentId;




    /**
    * 部门名称
    * @since 2025/11/27
    */
    @NotBlank(groups = AddGroup.class, message = "部门名称不能为空")
    private String deptName;




    /**
    * 显示顺序
    * @since 2025/11/27
    */
    private Integer orderNum;




    /**
    * 负责人
    * @since 2025/11/27
    */
    private String leader;




    /**
    * 联系电话
    * @since 2025/11/27
    */
    private String phone;




    /**
    * 邮箱
    * @since 2025/11/27
    */
    private String email;




    /**
    * 部门状态（0正常 1停用）
    * @since 2025/11/27
    */
    @NotBlank(groups = AddGroup.class, message = "部门状态（0正常 1停用）不能为空")
    private String status;




    /**
    * 删除标志（0代表存在 2代表删除）
    * @since 2025/11/27
    */
        @JsonIgnore
    private String delFlag;




    /**
    * 创建者
    * @since 2025/11/27
    */
        @JsonIgnore
    private String createBy;




    /**
    * 创建时间
    * @since 2025/11/27
    */
    private Date createTime;




    /**
    * 更新者
    * @since 2025/11/27
    */
        @JsonIgnore
    private String updateBy;




    /**
    * 更新时间
    * @since 2025/11/27
    */
        @JsonIgnore
    private Date updateTime;



}
