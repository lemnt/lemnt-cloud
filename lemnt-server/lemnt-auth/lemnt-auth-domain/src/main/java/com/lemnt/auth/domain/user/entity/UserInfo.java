package com.lemnt.auth.domain.user.entity;

import com.lemnt.auth.domain.user.gateway.PasswordEncoderGateway;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户对象 sys_user
 *
 * @author wangxx
 */
@Data
@Accessors(chain = true)
public class UserInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 6586525147787663980L;

    /**
     * 账号停用状态值
     */
    public static final String STATUS_DISABLE = "2";

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
     * 角色列表
     */
    private List<UserInfoRole> roles;

    /**
     * 账号是否已停用
     *
     * @return true 已停用
     */
    public boolean isDisabled() {
        return STATUS_DISABLE.equals(this.status);
    }

    /**
     * 校验明文密码是否匹配
     *
     * @param rawPassword 明文密码
     * @param encoder     密码编码器
     * @return true 匹配成功
     */
    public boolean verifyPassword(String rawPassword, PasswordEncoderGateway encoder) {
        return encoder.matches(rawPassword, this.password);
    }
}
