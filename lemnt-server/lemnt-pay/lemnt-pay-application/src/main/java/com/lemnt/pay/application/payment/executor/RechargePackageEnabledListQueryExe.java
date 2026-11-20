package com.lemnt.pay.application.payment.executor;

import com.lemnt.pay.application.payment.converter.RechargeDTOConverter;
import com.lemnt.pay.application.payment.dto.RechargePackageDTO;
import com.lemnt.pay.domain.payment.gateway.RechargePackageGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 启用中的充值套餐，供 App 选择。
 */
@Component
@RequiredArgsConstructor
public class RechargePackageEnabledListQueryExe {
    private final RechargePackageGateway packages;

    public List<RechargePackageDTO> execute() {
        return packages.listEnabled().stream().map(RechargeDTOConverter::packageView).toList();
    }
}
