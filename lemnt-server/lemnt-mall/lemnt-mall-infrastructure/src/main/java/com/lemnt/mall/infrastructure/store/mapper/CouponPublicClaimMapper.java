package com.lemnt.mall.infrastructure.store.mapper;

import com.lemnt.mall.infrastructure.store.dataobject.CouponPublicClaimDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** CouponPublicClaim 表数据访问接口。 */
@Mapper
public interface CouponPublicClaimMapper extends BaseMapper<CouponPublicClaimDO> {
    /**
     * 可领取。
     */
    @Select("SELECT enabled FROM mall_coupon_public_claim WHERE template_id=#{id}")
    Boolean claimable(Long id);

    /**
     * 设置可领取。
     */
    @Insert(
            "INSERT INTO mall_coupon_public_claim(template_id,enabled) VALUES(#{id},#{enabled}) ON"
            + " DUPLICATE KEY UPDATE enabled=#{enabled}")
    void setClaimable(@Param("id") Long id, @Param("enabled") boolean enabled);

    /**
     * 公开模板编号集合。
     */
    @Select(
            "SELECT template_id FROM mall_coupon_public_claim WHERE enabled=TRUE ORDER BY"
            + " template_id LIMIT 1001")
    List<Long> publicTemplateIds();
}
