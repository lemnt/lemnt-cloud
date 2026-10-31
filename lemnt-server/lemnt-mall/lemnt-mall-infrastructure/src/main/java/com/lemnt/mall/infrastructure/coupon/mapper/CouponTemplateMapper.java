package com.lemnt.mall.infrastructure.coupon.mapper;

import com.lemnt.mall.infrastructure.coupon.dataobject.CouponTemplateDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/** CouponTemplate 表数据访问接口。 */
@Mapper
public interface CouponTemplateMapper extends BaseMapper<CouponTemplateDO> {

    /**
     * 锁定模板。
     */
    CouponTemplateDO lockTemplate(Long id);

    /**
     * 扣减额度。
     */
    int takeQuota(Long id);

    /**
     * 可领取优惠。
     */
    List<CouponTemplateDO> offers(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 可领取优惠数量。
     */
    long offerCount();
    /** 获取数据库当前时间。 */
    LocalDateTime now();
}
