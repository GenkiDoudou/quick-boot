package com.su60.quickboot.system.entity;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;
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
@TableName("sys_dept")
public class SysDeptEntity implements Serializable{
@Serial
private static final long serialVersionUID = 1L;

        /**
        * 部门id
        * @since 2025/11/27
        */

        @TableId(value = "dept_id", type = IdType.ASSIGN_ID)
        private Long deptId;


        /**
        * 父部门id
        * @since 2025/11/27
        */
        @TableField("parent_id")
        private Long parentId;


        /**
        * 部门名称
        * @since 2025/11/27
        */
        @TableField("dept_name")
        private String deptName;


        /**
        * 显示顺序
        * @since 2025/11/27
        */
        @TableField("order_num")
        private Integer orderNum;


        /**
        * 负责人
        * @since 2025/11/27
        */
        @TableField("leader")
        private String leader;


        /**
        * 联系电话
        * @since 2025/11/27
        */
        @TableField("phone")
        private String phone;


        /**
        * 邮箱
        * @since 2025/11/27
        */
        @TableField("email")
        private String email;


        /**
        * 部门状态（0正常 1停用）
        * @since 2025/11/27
        */
        @TableField("status")
        private String status;


        /**
        * 删除标志（0代表存在 2代表删除）
        * @since 2025/11/27
        */
        @TableLogic
        private String delFlag;


        /**
        * 创建者
        * @since 2025/11/27
        */
        @TableField(value = "create_by", fill = FieldFill.INSERT)
        private String createBy;


        /**
        * 创建时间
        * @since 2025/11/27
        */
        @TableField(value = "create_time", fill = FieldFill.INSERT)
        private Date createTime;


        /**
        * 更新者
        * @since 2025/11/27
        */
        @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
        private String updateBy;


        /**
        * 更新时间
        * @since 2025/11/27
        */
        @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
        private Date updateTime;



}
