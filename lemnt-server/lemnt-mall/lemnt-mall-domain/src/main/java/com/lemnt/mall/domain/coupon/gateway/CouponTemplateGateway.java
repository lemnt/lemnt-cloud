package com.lemnt.mall.domain.coupon.gateway;

import com.lemnt.mall.domain.coupon.model.CouponTemplate;

import java.time.LocalDateTime;
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
    /** 更新当前模板业务规则，已发券不变。 */
    boolean updateTemplate(CouponTemplate value);

    /** 逻辑删除模板，已发券继续有效。 */
    boolean deleteTemplate(Long id);

    /** 调整发放/使用统计。 */
    void changeCounts(Long id, int takeDelta, int useDelta);

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
    /** 获取数据库时间，保持有效期判断与写入条件一致。 */
    LocalDateTime now();
    /** 是否开放会员主动领取。 */
    boolean claimable(Long id);

    /** 设置领取方式，不影响券模板本身启停。 */
    void claimable(Long id, boolean enabled);

    /** 读取已开放的模板编号，最多 1000 条。 */
    List<Long> publicTemplateIds();
}
