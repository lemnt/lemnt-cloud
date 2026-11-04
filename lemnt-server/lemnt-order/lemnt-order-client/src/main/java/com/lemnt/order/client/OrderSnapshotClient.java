package com.lemnt.order.client;

import com.lemnt.order.client.dto.BrokerageWithdrawalDTO;
import com.lemnt.order.client.dto.FrozenRefundDTO;
import com.lemnt.order.client.dto.OrderBenefitsSnapshotDTO;
import com.lemnt.order.client.dto.OrderPaymentSnapshotDTO;
import com.lemnt.order.client.dto.OrderPromotionResourcesDTO;

/** 可信内部支付/退款快照，写入协调始终由订单上下文拥有。 */
public interface OrderSnapshotClient {
    BrokerageWithdrawalDTO brokerageWithdrawal(Long id);

    OrderBenefitsSnapshotDTO benefitsSnapshot(String orderNo);

    /** 持有订单锁登记支付准备凭证后返回金额，防止跨库创建支付与调价竞争。 */
    OrderPaymentSnapshotDTO preparePayment(String orderNo);

    /** 原订单资源计划，活动、券及数量来自持久化快照。 */
    OrderPromotionResourcesDTO promotionResources(String orderNo);

    /** 读取原订单，不重新计算应付金额。 */
    OrderPaymentSnapshotDTO paymentOrder(String orderNo);

    /** 原售后审核通过后登记待执行请求；重复使用原售后号。operatorId为空表示系统。 */
    FrozenRefundDTO prepareRefund(String afterSaleNo, Long operatorId);

    /** 只读已冻结请求，不能从支付服务传入新金额覆盖。 */
    FrozenRefundDTO refundPlan(String afterSaleNo);
}
