package com.lemnt.pay.application.payment.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.pay.application.payment.converter.RechargeDTOConverter;
import com.lemnt.pay.application.payment.dto.RechargePackageDTO;
import com.lemnt.pay.application.payment.qry.RechargePackagePageQry;
import com.lemnt.pay.application.payment.support.PayConfigRules;
import com.lemnt.pay.domain.payment.gateway.RechargePackageGateway;
import com.lemnt.pay.domain.payment.model.RechargePackageFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 充值套餐分页查询。 */
@Component
@RequiredArgsConstructor
public class RechargePackagePageQueryExe {
    private final RechargePackageGateway packages;

    public PageResult<RechargePackageDTO> execute(RechargePackagePageQry qry) {
        if (qry == null) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        PayConfigRules.page(qry.pageNum(), qry.pageSize());
        var page =
                packages.page(
                        new RechargePackageFilter(
                                PayConfigRules.optional(qry.name(), 64, "套餐名"),
                                qry.status(),
                                qry.pageNum(),
                                qry.pageSize()));
        return PageResult.of(
                page.getRecords().stream().map(RechargeDTOConverter::packageView).toList(),
                page.getTotal());
    }
}
