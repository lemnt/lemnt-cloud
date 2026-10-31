package com.lemnt.auth.domain.member.gateway;

import com.lemnt.auth.domain.member.model.MemberLoginInfo;

/** 通过会员服务读取登录资料，认证中心不访问会员数据库。 */
public interface MemberAccountGateway {
    /** 获取会员登录资料，未找到时返回 null。 */
    MemberLoginInfo findByMobile(String mobile);

    /** 查询会员状态，会员不存在时返回 null。 */
    Integer findStatusByMemberId(Long memberId);
    /** 成功签发后由会员服务记录数据库登录时间。 */
    default void recordLogin(Long memberId) { recordLogin(memberId,null); }
    /** 同时写入本次连接IP，未知保持空。 */
    void recordLogin(Long memberId, String loginIp);
}
