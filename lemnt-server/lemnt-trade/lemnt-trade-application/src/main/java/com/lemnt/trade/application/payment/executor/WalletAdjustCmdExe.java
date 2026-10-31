package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.cmd.WalletAdjustCmd;
import com.lemnt.trade.application.payment.converter.WalletDTOConverter;
import com.lemnt.trade.application.payment.dto.WalletDTO;
import com.lemnt.trade.application.payment.service.WalletService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 后台人工调整钱包余额。 */
@Component
@RequiredArgsConstructor
public class WalletAdjustCmdExe {
    private final WalletService service;

    public WalletDTO execute(Long memberId, WalletAdjustCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("调整参数不合法");
        }
        return WalletDTOConverter.wallet(service.adjust(memberId, cmd.requestNo(), cmd.amount(), cmd.reason()));
    }
}
