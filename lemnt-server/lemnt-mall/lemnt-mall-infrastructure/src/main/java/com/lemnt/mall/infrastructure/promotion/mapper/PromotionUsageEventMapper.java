package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionUsageEventDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** PromotionUsageEvent 表数据访问接口。 */
@Mapper
public interface PromotionUsageEventMapper extends BaseMapper<PromotionUsageEventDO> {
    /**
     * 事件。
     */
    @Insert(
            "INSERT INTO"
            + " mall_promotion_usage_event(order_no,root_id,from_state,target_state,reason,occurred_at_epoch_ms)"
            + " VALUES(#{order},#{root},#{from},#{target},#{reason},#{now})")
    int event(
            @Param("order") String order,
            @Param("root") long root,
            @Param("from") String from,
            @Param("target") String target,
            @Param("reason") String reason,
            @Param("now") long now);
}
