package com.lemnt.member.infrastructure.address.gateway;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.address.gateway.MemberAddressGateway;
import com.lemnt.member.domain.address.model.Address;
import com.lemnt.member.infrastructure.address.converter.AddressConverter;
import com.lemnt.member.infrastructure.address.mapper.MemberAddressMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 会员收货地址应用编排，保持默认地址与数量约束的事务一致性。 */
@Component
@RequiredArgsConstructor
public class MemberAddressGatewayImpl implements MemberAddressGateway {

    private final MemberAddressMapper memberAddressMapper;

    /**
     * 查询收货地址。
     */
    @Override
    public List<Address> addresses(Long memberId) {
        return memberAddressMapper.addresses(memberId).stream().map(AddressConverter::toEntity).toList();
    }

    /**
     * 地址。
     */
    @Override
    public Address address(Long memberId, Long id) {
        return AddressConverter.toEntity(memberAddressMapper.address(memberId, id));
    }

    /**
     * 保存地址。
     */
    @Override
    public Long saveAddress(Address value) {
        var row = AddressConverter.toDO(value);
        if (value.id() == null) {
            memberAddressMapper.addressInsert(row);
        }
        else if (memberAddressMapper.addressUpdate(row) != 1) {
            throw new ServiceException("地址不存在", 409);
        }
        return row.getId();
    }

    /**
     * 删除地址。
     */
    @Override
    public boolean deleteAddress(Long memberId, Long id) {
        return memberAddressMapper.addressDelete(memberId, id) == 1;
    }

    /**
     * 清除默认地址。
     */
    @Override
    public void clearDefaultAddress(Long memberId) {
        memberAddressMapper.clearDefaultAddress(memberId);
    }
}
