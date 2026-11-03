package com.lemnt.order.client;

import com.lemnt.order.client.dto.FrozenRefundDTO;
import com.lemnt.order.client.dto.OrderPaymentSnapshotDTO;

/** 可信内部支付/退款快照，写入协调始终由订单上下文拥有。 */
public interface OrderSnapshotClient {
    /** 读取原订单，不重新计算应付金额。 */
    OrderPaymentSnapshotDTO paymentOrder(String orderNo);

    /** 原售后审核通过后登记待执行请求；重复使用原售后号。operatorId为空表示系统。 */
    FrozenRefundDTO prepareRefund(String afterSaleNo, Long operatorId);

    /** 只读已冻结请求，不能从支付服务传入新金额覆盖。 */
    FrozenRefundDTO refundPlan(String afterSaleNo);
}
