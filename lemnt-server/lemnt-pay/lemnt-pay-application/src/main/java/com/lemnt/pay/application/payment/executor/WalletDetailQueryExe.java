package com.lemnt.pay.application.payment.executor;

import com.lemnt.pay.application.payment.converter.WalletDTOConverter;
import com.lemnt.pay.application.payment.dto.WalletDTO;
import com.lemnt.pay.domain.payment.gateway.WalletGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 后台钱包详情查询，不存在返回空。
 */
@Component
@RequiredArgsConstructor
public class WalletDetailQueryExe {
    private final WalletGateway wallets;

    public WalletDTO execute(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("会员编号不合法");
        }
        return WalletDTOConverter.wallet(wallets.findByMemberId(memberId));
    }
}
