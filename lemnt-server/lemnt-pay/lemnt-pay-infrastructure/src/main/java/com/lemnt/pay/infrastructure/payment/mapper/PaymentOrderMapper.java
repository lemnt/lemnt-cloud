package com.lemnt.pay.infrastructure.payment.mapper;

import com.lemnt.pay.infrastructure.payment.dataobject.PaymentOrderDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 同一业务的开单、支付确认及订单关单共用主单行锁。 */
@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrderDO> {
    int createIfAbsent(PaymentOrderDO row);

    PaymentOrderDO lockByOrderNo(@Param("orderNo") String orderNo);

    int updateFunding(
            @Param("orderNo") String orderNo,
            @Param("status") String status,
            @Param("paidPaymentNo") String paidPaymentNo);
}
