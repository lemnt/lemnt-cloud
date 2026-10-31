package com.lemnt.member.client;

import com.lemnt.member.client.dto.MemberAddressDTO;

/** 仅按会员归属读取收货地址，不提供绕过归属的地址查询。 */
public interface MemberAddressClient {
    /** 地址不存在或不属于会员时返回 null。 */
    MemberAddressDTO get(Long memberId, Long addressId);
}
