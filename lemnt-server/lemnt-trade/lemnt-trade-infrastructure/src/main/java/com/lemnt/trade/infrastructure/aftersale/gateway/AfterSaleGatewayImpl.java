package com.lemnt.trade.infrastructure.aftersale.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.aftersale.model.AfterSale;
import com.lemnt.trade.domain.aftersale.model.AfterSaleFilter;
import com.lemnt.trade.infrastructure.aftersale.converter.AfterSaleConverter;
import com.lemnt.trade.infrastructure.aftersale.dataobject.AfterSaleDO;
import com.lemnt.trade.infrastructure.aftersale.mapper.AfterSaleMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/** 售后表持久化，不跨表更新订单或支付退款。 */
@Component
@RequiredArgsConstructor
public class AfterSaleGatewayImpl implements AfterSaleGateway {
    private final AfterSaleMapper mapper;

    @Override
    public PageResult<AfterSale> afterSalePage(AfterSaleFilter filter) {
        var page = mapper.selectPage(new Page<>((long) filter.offset() / filter.limit() + 1, filter.limit()),
                Wrappers.<AfterSaleDO>lambdaQuery().eq(filter.orderNo() != null, AfterSaleDO::getOrderNo, filter.orderNo())
                        .eq(filter.status() != null, AfterSaleDO::getStatus, filter.status())
                        .orderByDesc(AfterSaleDO::getRequestedAt).orderByDesc(AfterSaleDO::getId));
        return PageResult.of(page.getRecords().stream().map(AfterSaleConverter::toModel).toList(), page.getTotal());
    }

    @Override
    public AfterSale afterSale(String no) {
        return AfterSaleConverter.toModel(mapper.selectOne(Wrappers.<AfterSaleDO>lambdaQuery().eq(AfterSaleDO::getRefundNo, no)));
    }

    @Override
    public AfterSale lockAfterSale(String no) {
        return AfterSaleConverter.toModel(mapper.lockByNo(no));
    }

    @Override
    public List<AfterSale> afterSales(String orderNo) {
        return mapper.selectList(Wrappers.<AfterSaleDO>lambdaQuery().eq(AfterSaleDO::getOrderNo, orderNo)
                .orderByAsc(AfterSaleDO::getId)).stream().map(AfterSaleConverter::toModel).toList();
    }

    @Override
    public void saveAfterSale(AfterSale value) {
        mapper.insert(AfterSaleConverter.toDO(value));
    }

    @Override
    public boolean updateAfterSale(AfterSale value, String expectedStatus) {
        return mapper.update(null, Wrappers.<AfterSaleDO>lambdaUpdate()
                .eq(AfterSaleDO::getRefundNo, value.refundNo()).eq(AfterSaleDO::getStatus, expectedStatus)
                .set(AfterSaleDO::getStatus, value.status())
                .set(AfterSaleDO::getAuditUserId, value.auditUserId())
                .set(AfterSaleDO::getActiveOrderNo, value.orderItemId() == null
                        && Set.of("REQUESTED", "AWAITING_RETURN", "RETURNED", "WAIT_REFUND", "REFUNDING")
                                .contains(value.status()) ? value.orderNo() : null)
                .set(AfterSaleDO::getDecisionReason, value.decisionReason()).set(AfterSaleDO::getDecidedAt, value.decidedAt())
                .set(AfterSaleDO::getReturnCarrierCode, value.returnCarrierCode())
                .set(AfterSaleDO::getReturnTrackingNo, value.returnTrackingNo())
                .set(AfterSaleDO::getReturnShippedAt, value.returnShippedAt())
                .set(AfterSaleDO::getReturnReceivedAt, value.returnReceivedAt())) == 1;
    }

    @Override
    public boolean startPaymentRefund(String no) {
        return mapper.update(null, Wrappers.<AfterSaleDO>lambdaUpdate().eq(AfterSaleDO::getRefundNo, no)
                .eq(AfterSaleDO::getStatus, "WAIT_REFUND").set(AfterSaleDO::getStatus, "REFUNDING")) == 1;
    }

    @Override
    public boolean completePaymentRefund(String no, LocalDateTime time) {
        return mapper.update(null, Wrappers.<AfterSaleDO>lambdaUpdate().eq(AfterSaleDO::getRefundNo, no)
                .eq(AfterSaleDO::getStatus, "REFUNDING").set(AfterSaleDO::getStatus, "REFUNDED")
                .set(AfterSaleDO::getRefundTime, time).set(AfterSaleDO::getActiveOrderNo, null)) == 1;
    }

    @Override
    public boolean activeRefund(String orderNo) {
        return mapper.selectCount(Wrappers.<AfterSaleDO>lambdaQuery().eq(AfterSaleDO::getOrderNo, orderNo)
                .in(AfterSaleDO::getStatus, "REQUESTED", "AWAITING_RETURN", "RETURNED", "WAIT_REFUND", "REFUNDING")) > 0;
    }

    @Override
    public AfterSale findRequest(String orderNo, String requestNo) {
        return AfterSaleConverter.toModel(mapper.selectOne(Wrappers.<AfterSaleDO>lambdaQuery()
                .eq(AfterSaleDO::getOrderNo, orderNo).eq(AfterSaleDO::getRequestNo, requestNo)));
    }
}
