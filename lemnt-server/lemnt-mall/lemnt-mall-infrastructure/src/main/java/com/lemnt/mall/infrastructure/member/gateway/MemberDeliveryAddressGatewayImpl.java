package com.lemnt.mall.infrastructure.member.gateway;

import com.lemnt.mall.domain.member.gateway.MemberDeliveryAddressGateway;
import com.lemnt.mall.domain.member.model.MemberDeliveryAddress;
import com.lemnt.member.client.MemberAddressClient;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 收件信息来自会员服务，不接收客户端传入的姓名和地址文本。 */
@Component
public class MemberDeliveryAddressGatewayImpl implements MemberDeliveryAddressGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private MemberAddressClient client;

    @Override
    public MemberDeliveryAddress get(Long memberId, Long addressId) {
        var row = client.get(memberId, addressId);
        if (row == null || !memberId.equals(row.memberId()) || !addressId.equals(row.id())
        ) {
            return null;
        }
        return new MemberDeliveryAddress(row.id(), row.memberId(), row.name(), row.mobile(), row.areaId(),
                row.detailAddress());
    }
}
