package com.lemnt.pay.application.payment.executor;

import com.lemnt.pay.application.payment.converter.WalletDTOConverter;
import com.lemnt.pay.application.payment.dto.WalletDTO;
import com.lemnt.pay.application.payment.service.WalletService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** App 本人钱包查询，首次访问创建零余额账户。 */
@Component
@RequiredArgsConstructor
public class MemberWalletQueryExe {
    private final WalletService service;

    public WalletDTO execute(Long memberId) {
        return WalletDTOConverter.wallet(service.getOrCreate(memberId));
    }
}
