package com.lemnt.order.application.order.service;

import com.lemnt.order.application.brokerage.cmd.BrokerageWithdrawAuditCmd;
import com.lemnt.order.application.brokerage.service.BrokerageWithdrawService;
import com.lemnt.order.application.paymentrefund.executor.PaymentRefundCreateCmdExe;
import com.lemnt.order.domain.order.gateway.OrderRecoveryGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 崩溃恢复保留原幂等号，失败留在原持久化状态供下一轮或人工排查。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderRecoveryService {
    private final OrderRecoveryGateway pending;
    private final OrderRemoteService orders;
    private final PaymentRefundCreateCmdExe refunds;
    private final BrokerageWithdrawService withdrawals;

    public void execute() {
        for (var no : pending.pendingPaid(50))
            try {
                orders.paid(no);
            } catch (RuntimeException e) {
                log.warn("支付后处理待重试，orderNo={},error={}", no, e.getClass().getSimpleName());
            }
        for (var no : pending.pendingRefunds(50))
            try {
                refunds.execute(no);
            } catch (RuntimeException e) {
                log.warn("售后现金请求待重试，afterSaleNo={},error={}", no, e.getClass().getSimpleName());
            }
        for (var id : pending.pendingWithdrawals(50))
            try {
                withdrawals.audit(new BrokerageWithdrawAuditCmd(id, true, null, null));
            } catch (RuntimeException e) {
                log.warn("佣金提现入账待重试，id={},error={}", id, e.getClass().getSimpleName());
            }
    }
}
