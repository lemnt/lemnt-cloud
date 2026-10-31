package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.converter.RechargeDTOConverter;
import com.lemnt.trade.application.payment.dto.RechargePackageDTO;
import com.lemnt.trade.domain.payment.gateway.RechargePackageGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 启用中的充值套餐，供 App 选择。 */
@Component
@RequiredArgsConstructor
public class RechargePackageEnabledListQueryExe {
    private final RechargePackageGateway packages;

    public List<RechargePackageDTO> execute() {
        return packages.listEnabled().stream().map(RechargeDTOConverter::packageView).toList();
    }
}
