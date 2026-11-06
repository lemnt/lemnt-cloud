package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.member.client.MemberUserClient;
import com.lemnt.pay.domain.payment.gateway.PayerOpenIdGateway;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 通过会员服务读取已绑定的微信 openid；会员未绑定或服务不可用时返回空，不伪造标识。
 */
@Component
public class PayerOpenIdGatewayImpl implements PayerOpenIdGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private MemberUserClient memberUserClient;

    @Override
    public String findOpenId(Long memberId) {
        return memberId == null ? null : memberUserClient.getWechatOpenId(memberId);
    }
}
