package com.lemnt.system.infrastructure.org.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 部门表 sys_dept（仅含数据库字段）
 *
 * @author wangxx
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_dept")
public class DeptDO extends BaseDO {

    /**
     * 部门ID
     */
    @TableId(type = IdType.AUTO)
    private Long deptId;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 祖级列表
     */
    private String ancestors;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 部门状态（1正常 2停用）
     */
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic(delval = "2", value = "0")
    private String delFlag;
}
