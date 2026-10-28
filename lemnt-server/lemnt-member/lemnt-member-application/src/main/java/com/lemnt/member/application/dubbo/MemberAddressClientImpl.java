package com.lemnt.member.application.dubbo;

import com.lemnt.member.client.MemberAddressClient;
import com.lemnt.member.client.dto.MemberAddressDTO;
import com.lemnt.member.domain.address.gateway.MemberAddressGateway;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

/** 地址查询在会员服务内按会员归属过滤。 */
@DubboService
@RequiredArgsConstructor
public class MemberAddressClientImpl implements MemberAddressClient {
    private final MemberAddressGateway addresses;

    @Override
    public MemberAddressDTO get(Long memberId, Long addressId) {
        if (memberId == null || memberId <= 0 || addressId == null || addressId <= 0
        ) {
            return null;
        }
        var row = addresses.address(memberId, addressId);
        return row == null ? null : new MemberAddressDTO(row.id(), row.memberId(), row.name(),
                row.mobile(), row.areaId(), row.detailAddress());
    }
}
