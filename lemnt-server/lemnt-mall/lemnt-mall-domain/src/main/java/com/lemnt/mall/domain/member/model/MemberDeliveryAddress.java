package com.lemnt.mall.domain.member.model;

import java.io.Serializable;

/** 结算可用的会员收货地址。 */
public record MemberDeliveryAddress(
        Long id,
        Long memberId,
        String name,
        String mobile,
        Long areaId,
        String detailAddress
) implements Serializable {
}
