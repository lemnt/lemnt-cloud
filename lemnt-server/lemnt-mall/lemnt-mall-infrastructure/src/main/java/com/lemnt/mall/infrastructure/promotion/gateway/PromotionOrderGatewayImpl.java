package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.PromotionOrderGateway;
import com.lemnt.mall.domain.promotion.model.PromotionOrder;
import com.lemnt.mall.infrastructure.promotion.converter.PromotionOrderConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionOrderDO;
import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionOrderRefundDO;
import com.lemnt.mall.infrastructure.promotion.mapper.PromotionOrderMapper;
import com.lemnt.mall.infrastructure.promotion.mapper.PromotionOrderRefundMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 活动订单映射只操作本表，状态使用条件更新保证重复回调幂等。 */
@Component
@RequiredArgsConstructor
public class PromotionOrderGatewayImpl implements PromotionOrderGateway {
    private final PromotionOrderMapper mapper;
    private final PromotionOrderRefundMapper refundMapper;

    @Override
    public PromotionOrder findByOrderNo(String orderNo) {
        return PromotionOrderConverter.model(mapper.selectOne(Wrappers.<PromotionOrderDO>lambdaQuery()
                .eq(PromotionOrderDO::getOrderNo, orderNo)));
    }

    @Override
    public PromotionOrder findByRequest(Long memberId, String requestNo) {
        return PromotionOrderConverter.model(mapper.selectOne(Wrappers.<PromotionOrderDO>lambdaQuery()
                .eq(PromotionOrderDO::getMemberId, memberId)
                .eq(PromotionOrderDO::getRequestNo, requestNo)));
    }

    @Override
    public PromotionOrder lockByOrderNo(String orderNo) {
        return PromotionOrderConverter.model(mapper.lockByOrderNo(orderNo));
    }

    @Override
    public PromotionOrder save(PromotionOrder value) {
        var row = PromotionOrderConverter.row(value);
        mapper.insert(row);
        return PromotionOrderConverter.model(row);
    }

    @Override
    public boolean markReserved(String orderNo) {
        return mapper.update(null, Wrappers.<PromotionOrderDO>lambdaUpdate()
                .eq(PromotionOrderDO::getOrderNo, orderNo)
                .eq(PromotionOrderDO::getStatus, "CREATED")
                .set(PromotionOrderDO::getStatus, "RESERVED")) == 1;
    }

    @Override
    public boolean markPaid(String orderNo) {
        return mapper.update(null, Wrappers.<PromotionOrderDO>lambdaUpdate()
                .eq(PromotionOrderDO::getOrderNo, orderNo)
                .eq(PromotionOrderDO::getStatus, "RESERVED")
                .set(PromotionOrderDO::getStatus, "PAID")) == 1;
    }

    @Override
    public boolean markClosed(String orderNo) {
        return mapper.update(null, Wrappers.<PromotionOrderDO>lambdaUpdate()
                .eq(PromotionOrderDO::getOrderNo, orderNo)
                .in(PromotionOrderDO::getStatus, List.of("CREATED", "RESERVED"))
                .set(PromotionOrderDO::getStatus, "CLOSED")) == 1;
    }

    @Override
    public boolean markRefunded(String orderNo) {
        return mapper.update(null, Wrappers.<PromotionOrderDO>lambdaUpdate()
                .eq(PromotionOrderDO::getOrderNo, orderNo)
                .eq(PromotionOrderDO::getStatus, "PAID")
                .set(PromotionOrderDO::getStatus, "REFUNDED")) == 1;
    }

    @Override
    public boolean markFailed(String orderNo, String reason) {
        return mapper.update(null, Wrappers.<PromotionOrderDO>lambdaUpdate()
                .eq(PromotionOrderDO::getOrderNo, orderNo)
                .eq(PromotionOrderDO::getStatus, "CREATED")
                .set(PromotionOrderDO::getStatus, "FAILED")
                .set(PromotionOrderDO::getFailureReason, reason)) == 1;
    }

    @Override
    public boolean reopenFailed(String orderNo) {
        return mapper.update(null, Wrappers.<PromotionOrderDO>lambdaUpdate()
                .eq(PromotionOrderDO::getOrderNo, orderNo)
                .eq(PromotionOrderDO::getStatus, "FAILED")
                .set(PromotionOrderDO::getStatus, "CREATED")
                .set(PromotionOrderDO::getFailureReason, null)) == 1;
    }

    @Override
    public boolean bindRecord(String orderNo, Long recordId) {
        return mapper.update(null, Wrappers.<PromotionOrderDO>lambdaUpdate()
                .eq(PromotionOrderDO::getOrderNo, orderNo)
                .set(PromotionOrderDO::getActivityRecordId, recordId)) == 1;
    }

    @Override
    public boolean addRefundedQuantity(String orderNo, Integer expected, Integer quantity) {
        return mapper.update(null, Wrappers.<PromotionOrderDO>lambdaUpdate()
                .eq(PromotionOrderDO::getOrderNo, orderNo)
                .eq(PromotionOrderDO::getRefundedQuantity, expected)
                .set(PromotionOrderDO::getRefundedQuantity, expected + quantity)) == 1;
    }

    @Override
    public boolean recordRefund(String orderNo, String refundNo, Integer quantity) {
        var row = new PromotionOrderRefundDO();
        row.setOrderNo(orderNo);
        row.setRefundNo(refundNo);
        row.setQuantity(quantity);
        try {
            refundMapper.insert(row);
            return true;
        } catch (DuplicateKeyException exception) {
            return false;
        }
    }
}
