package com.lemnt.member.client;

import com.lemnt.member.client.dto.MemberLoginInfoDTO;
import com.lemnt.member.client.dto.MemberUserDTO;

/** 会员资料及认证中心专用登录资料读取契约，不处理令牌。 */
public interface MemberUserClient {

    /**
     * 根据会员编号查询基础资料。
     *
     * @param memberId 会员编号
     * @return 会员基础资料；会员不存在时返回 null
     */
    MemberUserDTO getByMemberId(Long memberId);

    /** 按手机号读取认证中心所需的登录资料，会员不存在时返回 null。 */
    MemberLoginInfoDTO getByMobile(String mobile);

    /** 认证成功后记录登录时间，失败不能作为成功登录返回。 */
    default void recordLogin(Long memberId) { recordLogin(memberId,null); }
    /** 同时写入本次连接IP，未知保持空。 */
    void recordLogin(Long memberId, String loginIp);
}
