package com.lemnt.order.domain.order.gateway;

import java.util.List;

/**
 * 读取持久化待处理事实，恢复只复用原订单、售后及提现编号。
 */
public interface OrderRecoveryGateway {
    List<String> pendingPaid(int size);

    List<String> pendingRefunds(int size);

    List<Long> pendingWithdrawals(int size);
}
