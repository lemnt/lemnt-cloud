package com.lemnt.mall.domain.member.gateway;

import com.lemnt.mall.domain.member.model.MemberDeliveryAddress;

/** 按会员归属读取收货地址。 */
public interface MemberDeliveryAddressGateway {
    /** 不存在或不属于会员时返回 null。 */
    MemberDeliveryAddress get(Long memberId, Long addressId);
}
