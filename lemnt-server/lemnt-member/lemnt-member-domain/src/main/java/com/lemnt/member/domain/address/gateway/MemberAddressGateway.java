package com.lemnt.member.domain.address.gateway;

import com.lemnt.member.domain.address.model.Address;

import java.util.List;

/** 会员收货地址领域网关。 */
public interface MemberAddressGateway {

    /**
     * 查询收货地址。
     */
    List<Address> addresses(Long memberId);

    /**
     * 地址。
     */
    Address address(Long memberId, Long id);

    /**
     * 保存地址。
     */
    Long saveAddress(Address value);

    /**
     * 删除地址。
     */
    boolean deleteAddress(Long memberId, Long id);

    /**
     * 清除默认地址。
     */
    void clearDefaultAddress(Long memberId);
}
