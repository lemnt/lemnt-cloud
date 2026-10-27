package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionUsageDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** PromotionUsage 表数据访问接口。 */
@Mapper
public interface PromotionUsageMapper extends BaseMapper<PromotionUsageDO> {
    /**
     * 锁定使用。
     */
    @Select(
            "SELECT * FROM mall_promotion_usage WHERE order_no=#{order} AND root_id=#{root} FOR"
            + " UPDATE")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    PromotionUsageDO lockUsage(@Param("order") String order, @Param("root") long root);

    /**
     * 插入使用。
     */
    @Insert(
            "INSERT INTO"
            + " mall_promotion_usage(order_no,root_id,version,member_id,discount_fen,rule_hash,state,created_at_epoch_ms,updated_at_epoch_ms)"
            + " VALUES(#{row.orderNo},#{row.rootId},#{row.version},#{row.memberId},#{row.discountFen},#{row.ruleHash},'HELD',#{now},#{now})")
    int insertUsage(@Param("row") PromotionUsageDO row, @Param("now") long now);

    /**
     * 完成使用。
     */
    default int finishUsage(
            String order,
            long root,
            String target,
            long now) {
        return this.update(null, Wrappers.<PromotionUsageDO>lambdaUpdate()
                .set(PromotionUsageDO::getState, target)
                .set(PromotionUsageDO::getUpdatedAtEpochMs, now)
                .eq(PromotionUsageDO::getOrderNo, order)
                .eq(PromotionUsageDO::getRootId, root)
                .eq(PromotionUsageDO::getState, "HELD"));
    }
}
