package com.lemnt.trade.infrastructure.paymentrefund.mapper;

import com.lemnt.trade.infrastructure.paymentrefund.dataobject.PaymentRefundDO;
import com.lemnt.trade.infrastructure.paymentrefund.dataobject.PaymentRefundTotalDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 支付退款表数据访问接口。 */
@Mapper
public interface PaymentRefundMapper extends BaseMapper<PaymentRefundDO> {
    /** 聚合成功支付退款记录。 */
    @Select("<script>SELECT COUNT(*) AS refund_count,"
            + " COALESCE(SUM(amount),0) AS refund_amount FROM trade_payment_refund WHERE status='SUCCESS'"
            + " <if test='orderNo != null'>AND order_no=#{orderNo}</if></script>")
    PaymentRefundTotalDO total(
            @Param("orderNo") String orderNo);

}
