package com.lemnt.member.infrastructure.address.gateway;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.address.gateway.MemberAddressGateway;
import com.lemnt.member.domain.address.model.Address;
import com.lemnt.member.infrastructure.address.converter.AddressConverter;
import com.lemnt.member.infrastructure.address.dataobject.AddressDO;
import com.lemnt.member.infrastructure.address.mapper.MemberAddressMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.List;

/** member_address逐表存储，所有操作保留本人归属条件。 */
@Component
@RequiredArgsConstructor
public class MemberAddressGatewayImpl implements MemberAddressGateway {

    private final MemberAddressMapper memberAddressMapper;

    /** 本人地址列表，默认地址优先。 */
    @Override
    public List<Address> addresses(Long memberId) {
        return memberAddressMapper.selectList(Wrappers.<AddressDO>lambdaQuery()
                .eq(AddressDO::getUserId, memberId)
                .orderByDesc(AddressDO::getDefaultStatus).orderByDesc(AddressDO::getId))
                .stream().map(AddressConverter::toEntity).toList();
    }

    /** 本人地址详情，缺失或他人记录均为空。 */
    @Override
    public Address address(Long memberId, Long id) {
        return AddressConverter.toEntity(memberAddressMapper.selectOne(Wrappers.<AddressDO>lambdaQuery()
                .eq(AddressDO::getUserId, memberId).eq(AddressDO::getId, id)));
    }

    /** 数据库过滤默认项，不加载全部地址。 */
    @Override
    public Address defaultAddress(Long memberId) {
        return AddressConverter.toEntity(memberAddressMapper.selectOne(Wrappers.<AddressDO>lambdaQuery()
                .eq(AddressDO::getUserId, memberId).eq(AddressDO::getDefaultStatus, true)
                .orderByDesc(AddressDO::getId).last("LIMIT 1")));
    }

    /** 复用BaseMapper插入/更新，实体触发统一审计填充。 */
    @Override
    public Long saveAddress(Address value) {
        var row = AddressConverter.toDO(value);
        if (value.id() == null) {
            memberAddressMapper.insert(row);
        } else if (memberAddressMapper.update(row, Wrappers.<AddressDO>lambdaUpdate()
                .eq(AddressDO::getId, value.id()).eq(AddressDO::getUserId, value.memberId())) != 1) {
            throw new ServiceException("地址不存在", 404);
        }
        return row.getId();
    }

    /** 原子删除本人地址，由应用层统一处理缺失。 */
    @Override
    public boolean deleteAddress(Long memberId, Long id) {
        return memberAddressMapper.delete(Wrappers.<AddressDO>lambdaQuery()
                .eq(AddressDO::getUserId, memberId).eq(AddressDO::getId, id)) == 1;
    }

    /** 只清除本人其他默认项，避免污染未变更地址的更新审计。 */
    @Override
    public void clearDefaultAddress(Long memberId, Long excludedId) {
        memberAddressMapper.update(new AddressDO(), Wrappers.<AddressDO>lambdaUpdate()
                .set(AddressDO::getDefaultStatus, false)
                .eq(AddressDO::getUserId, memberId).eq(AddressDO::getDefaultStatus, true)
                .ne(excludedId != null, AddressDO::getId, excludedId));
    }
}
