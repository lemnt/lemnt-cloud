package com.lemnt.mall.adapter.web.checkout.res;

import com.lemnt.mall.adapter.web.checkout.res.CheckoutItemRes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 结算报价响应。
 */
@Schema(description = "CheckoutQuote响应")
public record CheckoutQuoteRes(
        @Schema(description = "商品价格和数量快照")
        List<CheckoutItemRes> items,
        @Schema(description = "商品原金额，元")
        BigDecimal originalAmount,
        @Schema(description = "优惠金额，元")
        BigDecimal discountAmount,
        @Schema(description = "实付金额，元")
        BigDecimal payableAmount,
        @Schema(description = "优惠券实例编号，未选择时省略")
        Long couponId,
        @Schema(description = "提交时必须原样带回的报价令牌")
        String quoteToken
) implements Serializable {}
