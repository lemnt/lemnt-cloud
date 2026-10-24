package com.lemnt.auth.domain.user.gateway;

/**
 * 密码编码器领域接口
 * <p>
 * 将密码加密与校验算法（BCrypt / SCrypt 等）屏蔽在基础设施层，
 * 领域层只依赖此接口完成密码比对，不依赖任何加密框架。
 * </p>
 *
 * @author wangxx
 */
public interface PasswordEncoderGateway {

    /**
     * 校验明文是否匹配已加密的密码
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 已加密的密码
     * @return true 匹配成功
     */
    boolean matches(String rawPassword, String encodedPassword);

    /**
     * 加密明文密码
     *
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    String encode(String rawPassword);
}