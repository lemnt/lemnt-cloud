package com.lemnt.trade.infrastructure.paymentrefund.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefundFilter;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefundTotal;
import com.lemnt.trade.infrastructure.paymentrefund.converter.PaymentRefundConverter;
import com.lemnt.trade.infrastructure.paymentrefund.dataobject.PaymentRefundDO;
import com.lemnt.trade.infrastructure.paymentrefund.mapper.PaymentRefundMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/** 支付退款表存储实现。 */
@Component
@RequiredArgsConstructor
public class PaymentRefundGatewayImpl implements PaymentRefundGateway {
    private final PaymentRefundMapper mapper;

    @Override
    public PaymentRefundTotal total(String orderNo) {
        var value = mapper.total(orderNo);
        return new PaymentRefundTotal(value.refundCount(), value.refundAmount());
    }

    @Override
    public BigDecimal totalAmount(String paymentNo, String excludedRefundNo, boolean successOnly) {
        return mapper.selectList(Wrappers.lambdaQuery(PaymentRefundDO.class)
                .eq(PaymentRefundDO::getPaymentNo, paymentNo)
                .ne(excludedRefundNo != null, PaymentRefundDO::getRefundNo, excludedRefundNo)
                .eq(successOnly, PaymentRefundDO::getStatus, "SUCCESS"))
                .stream().map(PaymentRefundDO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public PaymentRefund find(String refundNo) {
        return PaymentRefundConverter.toModel(mapper.selectById(refundNo));
    }

    @Override
    public PaymentRefund findByAfterSaleNo(String afterSaleNo) {
        return PaymentRefundConverter.toModel(mapper.selectOne(Wrappers.lambdaQuery(PaymentRefundDO.class)
                .eq(PaymentRefundDO::getAfterSaleNo, afterSaleNo)));
    }

    @Override
    public void save(PaymentRefund refund) {
        mapper.insert(PaymentRefundConverter.toDO(refund));
    }

    @Override
    public boolean updateResult(PaymentRefund refund) {
        return mapper.update(null, Wrappers.lambdaUpdate(PaymentRefundDO.class)
                .eq(PaymentRefundDO::getRefundNo, refund.refundNo())
                .ne(PaymentRefundDO::getStatus, "SUCCESS")
                .set(PaymentRefundDO::getStatus, refund.status())
                .set(PaymentRefundDO::getChannelRefundNo, refund.channelRefundNo())
                .setSql("update_time=CURRENT_TIMESTAMP")) == 1;
    }

    @Override
    public PageResult<PaymentRefund> page(PaymentRefundFilter filter) {
        var page = mapper.selectPage(new Page<>(filter.pageNum(), filter.pageSize()),
                Wrappers.lambdaQuery(PaymentRefundDO.class)
                        .eq(filter.afterSaleNo() != null, PaymentRefundDO::getAfterSaleNo, filter.afterSaleNo())
                        .eq(filter.paymentNo() != null, PaymentRefundDO::getPaymentNo, filter.paymentNo())
                        .eq(filter.status() != null, PaymentRefundDO::getStatus, filter.status())
                        .orderByDesc(PaymentRefundDO::getCreateTime).orderByDesc(PaymentRefundDO::getRefundNo));
        return PageResult.of(
                page.getRecords().stream().map(PaymentRefundConverter::toModel).toList(), page.getTotal());
    }
}
