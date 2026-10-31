package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.converter.WalletTransferDTOConverter;
import com.lemnt.trade.application.payment.dto.WalletTransferDTO;
import com.lemnt.trade.domain.order.model.OrderRules;
import com.lemnt.trade.domain.payment.gateway.WalletTransferGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 钱包转账详情。 */
@Component
@RequiredArgsConstructor
public class WalletTransferDetailQueryExe {
    private final WalletTransferGateway transfers;

    public WalletTransferDTO execute(String transferNo) {
        return WalletTransferDTOConverter.transfer(transfers.findByNo(OrderRules.text(transferNo, 40)));
    }
}
