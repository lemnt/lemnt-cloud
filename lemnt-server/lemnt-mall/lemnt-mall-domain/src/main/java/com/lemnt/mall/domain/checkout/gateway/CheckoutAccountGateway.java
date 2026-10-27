package com.lemnt.mall.domain.checkout.gateway;

import java.time.LocalDateTime;

/** CheckoutAccount 表领域存储接口。 */
public interface CheckoutAccountGateway {
    /**
     * 获取数据库当前时间。
     */
    LocalDateTime now();

    /**
     * 锁定会员。
     */
    void lockMember(Long memberId);
}
