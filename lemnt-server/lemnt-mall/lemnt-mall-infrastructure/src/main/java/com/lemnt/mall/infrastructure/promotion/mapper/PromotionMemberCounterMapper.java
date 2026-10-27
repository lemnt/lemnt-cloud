package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionMemberCounterDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** PromotionMemberCounter 表数据访问接口。 */
@Mapper
public interface PromotionMemberCounterMapper extends BaseMapper<PromotionMemberCounterDO> {
    /**
     * 最大值会员使用。
     */
    @Select(
            "SELECT held_count+used_count FROM mall_promotion_member_counter WHERE root_id=#{id}"
            + " ORDER BY held_count+used_count DESC,member_id LIMIT 1 FOR UPDATE")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    Long largestMemberUsage(long id);

    /**
     * 确保会员计数器。
     */
    @Insert(
            "INSERT INTO mall_promotion_member_counter(root_id,member_id)"
            + " VALUES(#{root},#{member}) ON DUPLICATE KEY UPDATE member_id=member_id")
    int ensureMemberCounter(@Param("root") long root, @Param("member") long member);

    /**
     * 锁定会员计数器。
     */
    @Select(
            "SELECT * FROM mall_promotion_member_counter WHERE root_id=#{root} AND"
            + " member_id=#{member} FOR UPDATE")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    PromotionMemberCounterDO lockMemberCounter(@Param("root") long root, @Param("member") long member);

    /**
     * 获取会员。
     */
    @Update(
            "UPDATE mall_promotion_member_counter SET held_count=held_count+1 WHERE"
            + " root_id=#{root} AND member_id=#{member} AND"
            + " held_count<9223372036854775807-used_count AND (#{limit} IS NULL OR"
            + " held_count+used_count<#{limit})")
    int takeMember(
            @Param("root") long root, @Param("member") long member, @Param("limit") Long limit);

    /**
     * 完成会员。
     */
    @Update(
            "UPDATE mall_promotion_member_counter SET"
            + " held_count=held_count-1,used_count=used_count+CASE WHEN #{target}='USED' THEN 1"
            + " ELSE 0 END WHERE root_id=#{root} AND member_id=#{member} AND held_count>0")
    int finishMember(
            @Param("root") long root, @Param("member") long member, @Param("target") String target);
}
