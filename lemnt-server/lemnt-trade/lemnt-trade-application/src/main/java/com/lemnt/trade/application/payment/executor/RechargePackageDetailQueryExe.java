package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.converter.RechargeDTOConverter;
import com.lemnt.trade.application.payment.dto.RechargePackageDTO;
import com.lemnt.trade.domain.payment.gateway.RechargePackageGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 充值套餐详情。 */
@Component
@RequiredArgsConstructor
public class RechargePackageDetailQueryExe {
    private final RechargePackageGateway packages;

    public RechargePackageDTO execute(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("套餐编号不合法");
        }
        return RechargeDTOConverter.packageView(packages.find(id));
    }
}
