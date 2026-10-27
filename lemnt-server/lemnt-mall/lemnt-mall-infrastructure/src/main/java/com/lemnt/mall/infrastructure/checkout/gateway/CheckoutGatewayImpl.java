package com.lemnt.mall.infrastructure.checkout.gateway;

import com.lemnt.mall.domain.checkout.gateway.CheckoutGateway;
import com.lemnt.mall.domain.checkout.model.CheckoutRecord;
import com.lemnt.mall.infrastructure.checkout.converter.CheckoutConverter;
import com.lemnt.mall.infrastructure.checkout.dataobject.CheckoutDO;
import com.lemnt.mall.infrastructure.checkout.mapper.CheckoutMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** Checkout 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class CheckoutGatewayImpl implements CheckoutGateway {
    private final CheckoutMapper checkoutMapper;

    /** 读取提单请求主体。 */
    @Override
    public CheckoutRecord findRequest(Long memberId, String requestNo) {
        return CheckoutConverter.checkout(checkoutMapper.request(memberId, requestNo), List.of());
    }

    /** 读取结算回执主体。 */
    @Override
    public CheckoutRecord receipt(String orderNo) {
        return CheckoutConverter.checkout(checkoutMapper.receipt(orderNo), List.of());
    }

    /** 保存结算主体，购物车引用由应用层编排写入。 */
    @Override
    public void save(CheckoutRecord value) {
        var row = new CheckoutDO();
        row.setOrderNo(value.orderNo());
        row.setMemberId(value.memberId());
        row.setRequestNo(value.requestNo());
        row.setFingerprint(value.fingerprint());
        row.setOriginalAmount(value.originalAmount());
        row.setDiscountAmount(value.discountAmount());
        row.setPayableAmount(value.payableAmount());
        row.setCouponId(value.couponId());
        checkoutMapper.insertCheckout(row);
    }
}
