package com.lemnt.pay.application.dubbo;

import com.lemnt.pay.application.payment.service.WalletService;
import com.lemnt.pay.client.WalletClient;
import com.lemnt.pay.client.dto.WalletDTO;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

/** 钱包原账户快照，所有资金写入仍由既有账本服务串行处理。 */
@DubboService
@RequiredArgsConstructor
public class WalletClientImpl implements WalletClient {
    private final WalletService wallets;

    @Override
    public WalletDTO getOrCreate(Long memberId) {
        var value = wallets.getOrCreate(memberId);
        return new WalletDTO(
                value.memberId(),
                value.balance(),
                value.freezePrice(),
                value.totalExpense(),
                value.totalRecharge());
    }
}
