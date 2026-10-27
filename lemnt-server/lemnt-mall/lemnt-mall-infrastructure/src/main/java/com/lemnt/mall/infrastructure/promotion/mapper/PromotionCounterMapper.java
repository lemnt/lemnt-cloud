package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionCounterDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** PromotionCounter 表数据访问接口。 */
@Mapper
public interface PromotionCounterMapper extends BaseMapper<PromotionCounterDO> {
    /**
     * 插入计数器。
     */
    @Insert("INSERT INTO mall_promotion_counter(root_id) VALUES(#{id})")
    int insertCounter(long id);

    /**
     * 锁定计数器。
     */
    @Select("SELECT * FROM mall_promotion_counter WHERE root_id=#{id} FOR UPDATE")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    PromotionCounterDO lockCounter(long id);

    /**
     * 设置限制。
     */
    default int setLimits(
            long root, Long global, Long member) {
        return this.update(null, Wrappers.<PromotionCounterDO>lambdaUpdate()
                .set(PromotionCounterDO::getGlobalLimit, global)
                .set(PromotionCounterDO::getMemberLimit, member)
                .eq(PromotionCounterDO::getRootId, root));
    }

    /**
     * 获取全局预算。
     */
    @Update(
            "UPDATE mall_promotion_counter SET held_count=held_count+1 WHERE root_id=#{root} AND"
            + " held_count<9223372036854775807-used_count AND (global_limit IS NULL OR"
            + " held_count+used_count<global_limit)")
    int takeGlobal(long root);

    /**
     * 完成全局记录。
     */
    @Update(
            "UPDATE mall_promotion_counter SET held_count=held_count-1,used_count=used_count+CASE"
            + " WHEN #{target}='USED' THEN 1 ELSE 0 END WHERE root_id=#{root} AND held_count>0")
    int finishGlobal(@Param("root") long root, @Param("target") String target);
}
