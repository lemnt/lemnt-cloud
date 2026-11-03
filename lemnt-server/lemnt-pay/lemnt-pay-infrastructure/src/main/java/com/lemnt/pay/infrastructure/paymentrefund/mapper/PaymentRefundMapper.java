package com.lemnt.pay.infrastructure.paymentrefund.mapper;

import com.lemnt.pay.infrastructure.paymentrefund.dataobject.PaymentRefundDO;
import com.lemnt.pay.infrastructure.paymentrefund.dataobject.PaymentRefundTotalDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 支付退款表数据访问接口。 */
@Mapper
public interface PaymentRefundMapper extends BaseMapper<PaymentRefundDO> {
    /** 未成功退款的结果更新，时间沿用数据库时钟，成功事实不可覆盖。 */
    int updateResult(
            @Param("refundNo") String refundNo,
            @Param("status") String status,
            @Param("channelRefundNo") String channelRefundNo);

    /** 聚合成功支付退款记录。 */
    PaymentRefundTotalDO total(@Param("orderNo") String orderNo);
}
