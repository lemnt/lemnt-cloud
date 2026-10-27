package com.lemnt.member.application.address.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.address.gateway.MemberAddressGateway;
import com.lemnt.member.domain.address.model.Address;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberUserGateway;
import com.lemnt.member.domain.user.model.Member;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 会员收货地址应用编排，保持默认地址与数量约束的事务一致性。 */
@Component
@RequiredArgsConstructor
public class MemberAddressService {

    private final MemberUserGateway memberUserGateway;

    private final MemberAddressGateway memberAddressGateway;

    private Member lock(Long id) {
        MemberRules.id(id);
        var value = memberUserGateway.lockById(id);
        if (value == null) {
            throw new ServiceException("会员不存在", 400);
        }
        return value;
    }

    private Member active(Long id) {
        var value = lock(id);
        if (!Integer.valueOf(1).equals(value.status())) {
            throw new ServiceException("会员已停用", 401);
        }
        return value;
    }

    /**
     * 保存地址。
     */
    @Transactional(rollbackFor = Exception.class)
    public Address saveAddress(Address input) {
        active(input.memberId());
        String name = MemberRules.text(input.name(), 80);
        String mobile = MemberRules.mobile(input.mobile());
        MemberRules.id(input.areaId());
        String detail = MemberRules.text(input.detailAddress(), 500);
        if (input.defaultStatus() == null) {
            throw new ServiceException("默认地址标志不能为空", 400);
        }
        if (input.id() != null
                && memberAddressGateway.address(input.memberId(), MemberRules.id(input.id())) == null) {
            throw new ServiceException("地址不存在", 400);
        }
        if (input.id() == null && memberAddressGateway.addresses(input.memberId()).size() >= 20) {
            throw new ServiceException("最多20个地址", 409);
        }
        if (input.defaultStatus()) {
            memberAddressGateway.clearDefaultAddress(input.memberId());
        }
        Long id =
                memberAddressGateway.saveAddress(
                        new Address(
                                input.id(),
                                input.memberId(),
                                name,
                                mobile,
                                input.areaId(),
                                detail,
                                input.defaultStatus()));
        return memberAddressGateway.address(input.memberId(), id);
    }

    /**
     * 删除地址。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long memberId, Long id) {
        active(memberId);
        return memberAddressGateway.deleteAddress(memberId, MemberRules.id(id));
    }

}
