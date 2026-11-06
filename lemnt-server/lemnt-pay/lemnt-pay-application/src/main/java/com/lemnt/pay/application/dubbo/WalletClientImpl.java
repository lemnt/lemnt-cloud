package com.lemnt.pay.application.dubbo;

import com.lemnt.pay.application.payment.service.WalletService;
import com.lemnt.pay.client.WalletClient;
import com.lemnt.pay.client.bo.BrokerageWithdrawalBo;
import com.lemnt.pay.client.dto.WalletDTO;
import com.lemnt.pay.domain.order.gateway.BrokerageWithdrawalGateway;
import com.lemnt.pay.domain.payment.model.WalletBizType;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 钱包原账户快照，所有资金写入仍由既有账本服务串行处理。
 */
@DubboService
@RequiredArgsConstructor
public class WalletClientImpl implements WalletClient {
    private final WalletService wallets;
    private final BrokerageWithdrawalGateway approvals;

    public void creditBrokerageWithdrawal(BrokerageWithdrawalBo v) {
        if (v == null
            || v.id() == null
            || v.id() < 1
            || v.amount() == null
            || v.amount().signum() <= 0) throw new IllegalArgumentException("提现入账参数不合法");
        var plan = approvals.find(v.id());
        if (plan == null
            || !v.memberId().equals(plan.memberId())
            || !Integer.valueOf(1).equals(plan.type())
            || !Set.of(10, 11).contains(plan.status())
            || plan.amount().compareTo(v.amount()) != 0)
            throw new IllegalStateException("原佣金提现审核事实不一致");
        wallets.credit(
            v.memberId(),
            WalletBizType.BROKERAGE_WITHDRAW,
            "BW" + v.id(),
            plan.amount(),
            "佣金提现");
    }

    @Override
    public WalletDTO getOrCreate(Long memberId) {
        var value = wallets.getOrCreate(memberId);
        return new WalletDTO(
            value.memberId(),
            value.balance(),
            value.freezePrice(),
            value.totalExpense(),
            value.totalRecharge());
    }
}
