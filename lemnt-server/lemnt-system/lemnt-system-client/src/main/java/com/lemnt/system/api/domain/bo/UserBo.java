package com.lemnt.system.api.domain.bo;

import com.lemnt.common.api.domain.bo.BaseBO;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 用户对象 sys_user
 *
 * @author wangxx
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class UserBo extends BaseBO {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 用户性别
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
     * 角色组
     */
    private Long[] roleIds;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色
     */
    private String roleKey;
}
