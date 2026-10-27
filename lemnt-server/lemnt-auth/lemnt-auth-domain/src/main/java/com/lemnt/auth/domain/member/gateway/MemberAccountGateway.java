package com.lemnt.auth.domain.member.gateway;

import com.lemnt.auth.domain.member.model.MemberLoginInfo;

/** 通过会员服务读取登录资料，认证中心不访问会员数据库。 */
public interface MemberAccountGateway {
    /** 获取会员登录资料，未找到时返回 null。 */
    MemberLoginInfo findByMobile(String mobile);

    /** 查询会员状态，会员不存在时返回 null。 */
    Integer findStatusByMemberId(Long memberId);
}
