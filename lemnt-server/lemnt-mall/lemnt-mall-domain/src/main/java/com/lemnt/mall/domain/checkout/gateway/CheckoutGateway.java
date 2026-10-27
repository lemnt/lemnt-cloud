package com.lemnt.mall.domain.checkout.gateway;

import com.lemnt.mall.domain.checkout.model.CheckoutRecord;

/** Checkout 表领域存储接口。 */
public interface CheckoutGateway {
    /** 读取提单请求主体。 */
    CheckoutRecord findRequest(Long memberId, String requestNo);

    /** 读取结算回执主体。 */
    CheckoutRecord receipt(String orderNo);

    /** 保存结算主体，购物车引用由应用层编排写入。 */
    void save(CheckoutRecord value);
}
