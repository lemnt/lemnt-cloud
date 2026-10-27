package com.lemnt.mall.infrastructure.checkout.mapper;

import com.lemnt.mall.infrastructure.checkout.dataobject.CouponTemplateDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** CouponTemplate 表数据访问接口。 */
@Mapper
public interface CouponTemplateMapper extends BaseMapper<CouponTemplateDO> {
    String Storefront_OFFER = " FROM mall_coupon_template t JOIN mall_coupon_public_claim p ON p.template_id=t.id"
            + " WHERE p.enabled=TRUE AND t.enabled=TRUE AND t.valid_from<=CURRENT_TIMESTAMP AND"
            + " t.valid_until>CURRENT_TIMESTAMP ";

    /**
     * 查询模板。
     */
    default List<CouponTemplateDO> templates() {
        return this.selectList(Wrappers.<CouponTemplateDO>lambdaQuery()
                .orderByDesc(CouponTemplateDO::getId)
                .last("LIMIT 1001"));
    }

    /**
     * 模板。
     */
    default CouponTemplateDO template(Long id) {
        return this.selectOne(Wrappers.<CouponTemplateDO>lambdaQuery()
                .eq(CouponTemplateDO::getId, id));
    }

    /**
     * 锁定模板。
     */
    @Select("SELECT * FROM mall_coupon_template WHERE id=#{id} FOR UPDATE")
    CouponTemplateDO lockTemplate(Long id);

    /**
     * 插入模板。
     */
    @Insert(
            "INSERT INTO"
            + " mall_coupon_template(code,name,kind,threshold,fixed_amount,discount_percent,discount_limit,total_count,issued_count,valid_from,valid_until,enabled)"
            + " VALUES(#{code},#{name},#{kind},#{threshold},#{fixedAmount},#{discountPercent},#{discountLimit},#{totalCount},0,#{validFrom},#{validUntil},TRUE)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTemplate(CouponTemplateDO row);

    /**
     * 启用模板。
     */
    default int enableTemplate(Long id, boolean enabled) {
        return this.update(null, Wrappers.<CouponTemplateDO>lambdaUpdate()
                .set(CouponTemplateDO::getEnabled, enabled)
                .eq(CouponTemplateDO::getId, id));
    }

    /**
     * 扣减额度。
     */
    @Update(
            "UPDATE mall_coupon_template SET issued_count=issued_count+1 WHERE id=#{id} AND"
            + " enabled=TRUE AND valid_from<=CURRENT_TIMESTAMP AND"
            + " valid_until>CURRENT_TIMESTAMP AND issued_count<total_count")
    int takeQuota(Long id);

    /**
     * 可领取优惠。
     */
    @Select("SELECT t.*" + Storefront_OFFER + " ORDER BY t.id DESC LIMIT #{limit} OFFSET #{offset}")
    List<CouponTemplateDO> offers(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 可领取优惠数量。
     */
    @Select("SELECT COUNT(*)" + Storefront_OFFER)
    long offerCount();
}
