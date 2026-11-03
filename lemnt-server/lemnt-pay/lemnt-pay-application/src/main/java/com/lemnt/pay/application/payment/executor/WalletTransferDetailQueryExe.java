package com.lemnt.pay.application.payment.executor;

import com.lemnt.pay.application.payment.converter.WalletTransferDTOConverter;
import com.lemnt.pay.application.payment.dto.WalletTransferDTO;
import com.lemnt.pay.domain.payment.gateway.WalletTransferGateway;
import com.lemnt.pay.domain.payment.model.PaymentRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 钱包转账详情。 */
@Component
@RequiredArgsConstructor
public class WalletTransferDetailQueryExe {
    private final WalletTransferGateway transfers;

    public WalletTransferDTO execute(String transferNo) {
        return WalletTransferDTOConverter.transfer(
                transfers.findByNo(PaymentRules.text(transferNo, 40)));
    }
}
