package com.lemnt.order.application.brokerage.service;

import com.lemnt.order.domain.brokerage.gateway.BrokerageRecordGateway;
import com.lemnt.order.domain.brokerage.gateway.BrokerageUserGateway;
import com.lemnt.order.domain.brokerage.model.BrokerageUser;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 单条佣金解冻：冻结佣金转入可用佣金，CAS 防止重复解冻。 */
@Component
@RequiredArgsConstructor
public class BrokerageSettlementService {
    private final BrokerageRecordGateway records;
    private final BrokerageUserGateway users;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean unfreeze(Long recordId) {
        var record = records.find(recordId);
        if (record == null
                || record.status() != BrokerageService.STATUS_WAIT
                || !records.updateStatus(
                        recordId, BrokerageService.STATUS_WAIT, BrokerageService.STATUS_SETTLED)) {
            return false;
        }
        var user = users.lock(record.userId());
        if (user == null || user.frozenPrice().compareTo(record.price()) < 0) {
            throw new IllegalStateException("冻结佣金不足，无法解冻");
        }
        BrokerageUser updated =
                new BrokerageUser(
                        user.id(),
                        user.bindUserId(),
                        user.bindUserTime(),
                        user.brokerageEnabled(),
                        user.brokerageTime(),
                        user.brokeragePrice().add(record.price()),
                        user.frozenPrice().subtract(record.price()));
        users.save(updated);
        return true;
    }
}
