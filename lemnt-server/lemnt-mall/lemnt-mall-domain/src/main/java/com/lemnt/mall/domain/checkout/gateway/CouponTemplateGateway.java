package com.lemnt.mall.domain.checkout.gateway;

import com.lemnt.mall.domain.checkout.model.CouponTemplate;

import java.util.List;

/** CouponTemplate 表领域存储接口。 */
public interface CouponTemplateGateway {
    /**
     * 查询模板。
     */
    List<CouponTemplate> templates();

    /**
     * 模板。
     */
    CouponTemplate template(Long id);

    /**
     * 锁定模板。
     */
    CouponTemplate lockTemplate(Long id);

    /**
     * 创建模板。
     */
    CouponTemplate createTemplate(CouponTemplate template);

    /**
     * 启用模板。
     */
    boolean enableTemplate(Long id, boolean enabled);

    /**
     * 可领取优惠。
     */
    List<CouponTemplate> offers(int offset, int limit);

    /**
     * 可领取优惠数量。
     */
    long offerCount();

    /** 占用优惠券发放名额。 */
    boolean takeQuota(Long templateId);
}
