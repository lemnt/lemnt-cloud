package com.lemnt.member.client.dto;

import java.io.Serializable;

/**
 * 跨服务读取的会员基础资料，不包含联系方式、密码或登录令牌。
 *
 * @param id 会员编号
 * @param memberNo 会员业务编号
 * @param nickname 昵称
 * @param status 状态：0停用，1启用
 */
public record MemberUserDTO(
        Long id,
        String memberNo,
        String nickname,
        Integer status
) implements Serializable {}
