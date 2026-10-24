package com.lemnt.system.domain.auth.model;

import com.lemnt.system.domain.org.model.Dept;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 用户聚合根实体
 *
 * @author wangxx
 */
@Data
@Accessors(chain = true)
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 7128427617790296597L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    private String sex;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 密码
     */
    private String password;

    /**
     * 账号状态（1正常 2停用）
     */
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private LocalDateTime loginDate;

    /**
     * 密码最后更新时间
     */
    private LocalDateTime pwdUpdateDate;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 备注
     */
    private String remark;

    /**
     * 角色组
     */
    private List<Role> roles;

    /**
     * 部门
     */
    private Dept dept;

    /**
     * 数据范围（关联，非DB字段）
     */
    private String dataScope;

    /**
     * 角色ID（查询用）
     */
    private Long roleId;

    /**
     * 开始时间（查询用）
     */
    private LocalDateTime beginTime;

    /**
     * 结束时间（查询用）
     */
    private LocalDateTime endTime;

    public boolean isAdmin() {
        return Objects.equals(this.userId, 1L);
    }
}