package com.lemnt.trade.domain.paymentrefund.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefundFilter;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefundTotal;

import java.math.BigDecimal;

/** 支付退款表存储接口。 */
public interface PaymentRefundGateway {
    /** 统计真实成功退款笔数与金额，不把待退款或售后申请算作已退款。 */
    PaymentRefundTotal total(String orderNo);

    /** 统计原支付占用或已成功退款金额，排除当前交易。 */
    BigDecimal totalAmount(String paymentNo, String excludedRefundNo, boolean successOnly);

    /** 按退款交易编号查询。 */
    PaymentRefund find(String refundNo);

    /** 按售后单查询唯一的退款交易。 */
    PaymentRefund findByAfterSaleNo(String afterSaleNo);

    /** 保存首次退款交易，调用方持有订单锁。 */
    void save(PaymentRefund refund);

    /** 更新未成功交易的渠道处理结果，不允许覆盖成功结果。 */
    boolean updateResult(PaymentRefund refund);

    /** 分页查询退款交易。 */
    PageResult<PaymentRefund> page(PaymentRefundFilter filter);
}
