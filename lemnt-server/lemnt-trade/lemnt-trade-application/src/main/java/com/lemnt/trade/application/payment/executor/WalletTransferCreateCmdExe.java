package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.cmd.WalletTransferCreateCmd;
import com.lemnt.trade.application.payment.converter.WalletTransferDTOConverter;
import com.lemnt.trade.application.payment.dto.WalletTransferDTO;
import com.lemnt.trade.application.payment.service.WalletTransferService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 会员发起钱包转账。 */
@Component
@RequiredArgsConstructor
public class WalletTransferCreateCmdExe {
    private final WalletTransferService service;

    public WalletTransferDTO execute(Long fromMemberId, WalletTransferCreateCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("转账参数不合法");
        }
        return WalletTransferDTOConverter.transfer(service.transfer(fromMemberId, cmd.toMemberId(), cmd.amount(),
                cmd.remark(), cmd.requestNo()));
    }
}
