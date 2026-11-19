package com.lemnt.pay.infrastructure.payment.mapper;

import com.lemnt.pay.domain.payment.model.PaymentTrendDay;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PaymentTrendMapper {
    /** 按支付及充值各自的到账日期聚合，避免跨记录相乘。 */
    List<PaymentTrendDay> days(
            @Param("from") LocalDateTime from, @Param("until") LocalDateTime until);
}
