package com.lemnt.order.application.brokerage.service;

import com.lemnt.order.domain.brokerage.gateway.BrokerageRecordGateway;
import com.lemnt.order.domain.brokerage.gateway.BrokerageUserGateway;
import com.lemnt.order.domain.brokerage.model.BrokerageUser;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * 单条佣金解冻：冻结佣金转入可用佣金，CAS 防止重复解冻。
 */
@Component
@RequiredArgsConstructor
public class BrokerageSettlementService {
    private final BrokerageRecordGateway records;
    private final BrokerageUserGateway users;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean unfreeze(Long recordId) {
        var record = records.lock(recordId);
        if (record == null
                || record.status() != BrokerageService.STATUS_WAIT
                || !records.updateStatus(
                        recordId, BrokerageService.STATUS_WAIT, BrokerageService.STATUS_SETTLED)) {
            return false;
        }
        var reversal =
                record.bizType() == BrokerageService.BIZ_ORDER
                        ? records.findByBiz(
                                record.userId(), BrokerageService.BIZ_ORDER_REFUND, record.bizId())
                        : null;
        // 部分退款已经从冻结账户扣回，解冻只转移原记录剩余金额。
        var amount = reversal == null ? record.price() : record.price().add(reversal.price());
        if (amount.signum() < 0 || amount.compareTo(record.price()) > 0) {
            throw new IllegalStateException("累计扣回佣金超出原发放记录");
        }
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        var user = users.lock(record.userId());
        if (user == null || user.frozenPrice().compareTo(amount) < 0) {
            throw new IllegalStateException("冻结佣金不足，无法解冻");
        }
        BrokerageUser updated =
                new BrokerageUser(
                        user.id(),
                        user.bindUserId(),
                        user.bindUserTime(),
                        user.brokerageEnabled(),
                        user.brokerageTime(),
                        user.brokeragePrice().add(amount),
                        user.frozenPrice().subtract(amount));
        users.save(updated);
        return true;
    }
}
