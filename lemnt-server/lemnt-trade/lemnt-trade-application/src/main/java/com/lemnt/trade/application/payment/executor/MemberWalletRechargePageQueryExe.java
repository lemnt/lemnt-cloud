package com.lemnt.trade.application.payment.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.payment.converter.RechargeDTOConverter;
import com.lemnt.trade.application.payment.dto.WalletRechargeDTO;
import com.lemnt.trade.application.payment.support.PayConfigRules;
import com.lemnt.trade.application.payment.support.RechargeRules;
import com.lemnt.trade.domain.payment.gateway.WalletRechargeGateway;
import com.lemnt.trade.domain.payment.model.WalletRechargeFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** App 本人充值单分页。 */
@Component
@RequiredArgsConstructor
public class MemberWalletRechargePageQueryExe {
    private final WalletRechargeGateway recharges;

    public PageResult<WalletRechargeDTO> execute(Long memberId, String status, int pageNum, int pageSize) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("会员编号不合法");
        }
        PayConfigRules.page(pageNum, pageSize);
        var page = recharges.page(new WalletRechargeFilter(memberId, RechargeRules.statusText(status), pageNum,
                pageSize));
        return PageResult.of(page.getRecords().stream().map(RechargeDTOConverter::recharge).toList(),
                page.getTotal());
    }
}
