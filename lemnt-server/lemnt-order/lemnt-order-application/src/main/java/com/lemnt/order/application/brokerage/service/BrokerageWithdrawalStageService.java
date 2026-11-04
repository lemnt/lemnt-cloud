package com.lemnt.order.application.brokerage.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.order.application.brokerage.cmd.BrokerageWithdrawAuditCmd;
import com.lemnt.order.domain.brokerage.gateway.BrokerageWithdrawGateway;
import com.lemnt.order.domain.brokerage.model.BrokerageWithdraw;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/** 审核通过先持久化10状态，远端结果未知时只重试入账，不能改为驳回返还佣金。 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class BrokerageWithdrawalStageService {
    private final BrokerageWithdrawGateway withdraws;
    private final BrokerageService brokerage;

    public BrokerageWithdraw prepare(BrokerageWithdrawAuditCmd cmd) {
        if (cmd == null || cmd.id() == null || cmd.id() < 1 || cmd.approved() == null)
            throw new ServiceException("审核参数不合法", 400);
        var v = withdraws.lock(cmd.id());
        if (v == null) throw new ServiceException("提现单不存在", 400);
        if (cmd.approved()) {
            if (v.status() == 11 || v.status() == 10) return v;
            if (v.status() != 0
                    || !withdraws.updateStatus(v.id(), 0, 10, cmd.reason(), LocalDateTime.now()))
                throw new ServiceException("提现单状态不允许审核", 400);
        } else {
            if (v.status() == 20) return v;
            if (v.status() != 0
                    || !withdraws.updateStatus(v.id(), 0, 20, cmd.reason(), LocalDateTime.now()))
                throw new ServiceException("已开始入账的提现不能驳回", 400);
            brokerage.returnRejectedWithdraw(v.userId(), v.id(), v.price());
        }
        return withdraws.find(v.id());
    }

    public void complete(Long id) {
        var v = withdraws.lock(id);
        if (v == null) throw new ServiceException("提现单不存在", 400);
        if (v.status() == 11) return;
        if (v.status() != 10 || !withdraws.updateStatus(id, 10, 11, v.auditReason(), v.auditTime()))
            throw new ServiceException("提现单状态已变化", 400);
    }
}
