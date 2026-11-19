package com.lemnt.order.infrastructure.order.mapper;

import com.lemnt.order.domain.order.model.PaymentRecoveryCandidate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 有界恢复查询，跨表条件在XML，不读取其他服务的表。
 */
@Mapper
public interface OrderRecoveryMapper {
    List<PaymentRecoveryCandidate> pendingPaymentConfirmation(
            @Param("afterId") long afterId, @Param("size") int size);

    List<String> pendingPaid(@Param("size") int size);

    List<String> pendingRefunds(@Param("size") int size);

    List<Long> pendingWithdrawals(@Param("size") int size);
}
