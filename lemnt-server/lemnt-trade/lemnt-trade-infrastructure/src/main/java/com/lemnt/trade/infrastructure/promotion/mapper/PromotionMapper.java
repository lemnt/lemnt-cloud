package com.lemnt.trade.infrastructure.promotion.mapper;

import com.lemnt.trade.infrastructure.promotion.dataobject.*;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PromotionMapper {
    String FILTER =
            "<where><if test='state != null'>state=#{state}</if><if test='code != null'> AND"
                    + " code=#{code}</if></where>";

    @Select(
            "<script>SELECT id,state,revision,active_version,last_version FROM trade_promotion_root"
                    + " "
                    + FILTER
                    + " ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}</script>")
    java.util.List<PromotionRootDO> page(
            com.lemnt.trade.domain.promotion.model.PromotionFilter filter);

    @Select("<script>SELECT COUNT(*) FROM trade_promotion_root " + FILTER + "</script>")
    long count(com.lemnt.trade.domain.promotion.model.PromotionFilter filter);

    @Select("SELECT UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000.0")
    long nowEpochMs();

    @Insert(
            "INSERT INTO"
                + " trade_promotion_admin_request(operator_id,request_no,action,fingerprint,created_at_epoch_ms)"
                + " VALUES(#{operator},#{request},#{action},#{fingerprint},UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000.0)"
                + " ON DUPLICATE KEY UPDATE request_no=request_no")
    int ensureRequest(
            @Param("operator") long operator,
            @Param("request") String request,
            @Param("action") String action,
            @Param("fingerprint") String fingerprint);

    @Select(
            "SELECT * FROM trade_promotion_admin_request WHERE operator_id=#{operator} AND"
                    + " request_no=#{request} FOR UPDATE")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    PromotionRequestDO lockRequest(
            @Param("operator") long operator, @Param("request") String request);

    @Update(
            "UPDATE trade_promotion_admin_request SET"
                + " completed_at_epoch_ms=UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000.0,result_id=#{receipt.id},result_revision=#{receipt.revision},result_state=#{receipt.state},result_active_version=#{receipt.activeVersion},result_last_version=#{receipt.lastVersion}"
                + " WHERE operator_id=#{operator} AND request_no=#{request} AND result_id IS NULL")
    int completeRequest(
            @Param("operator") long operator,
            @Param("request") String request,
            @Param("receipt") PromotionRootDO receipt);

    @Insert(
            "INSERT INTO trade_promotion_root(code,draft_json,created_by,updated_by)"
                    + " VALUES(#{row.code},#{row.draftJson},#{operator},#{operator})")
    @Options(useGeneratedKeys = true, keyProperty = "row.id")
    int insertRoot(@Param("row") PromotionRootDO row, @Param("operator") long operator);

    @Insert("INSERT INTO trade_promotion_counter(root_id) VALUES(#{id})")
    int insertCounter(long id);

    @Select("SELECT * FROM trade_promotion_root WHERE id=#{id}")
    PromotionRootDO root(long id);

    @Select("SELECT * FROM trade_promotion_root WHERE id=#{id} FOR UPDATE")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    PromotionRootDO lockRoot(long id);

    @Update(
            "UPDATE trade_promotion_root SET"
                + " state=#{row.state},revision=#{row.revision},active_version=#{row.activeVersion},last_version=#{row.lastVersion},draft_json=#{row.draftJson},updated_by=#{operator}"
                + " WHERE id=#{row.id} AND revision=#{expected}")
    int saveRoot(
            @Param("row") PromotionRootDO row,
            @Param("expected") long expected,
            @Param("operator") long operator);

    @Insert(
            "INSERT INTO"
                + " trade_promotion_version(root_id,version,draft_json,rule_hash,algorithm_version,starts_at_epoch_ms,ends_at_epoch_ms,operator_id,published_at_epoch_ms)"
                + " VALUES(#{rootId},#{version},#{draftJson},#{ruleHash},#{algorithmVersion},#{startsAtEpochMs},#{endsAtEpochMs},#{operatorId},#{publishedAtEpochMs})")
    int insertVersion(PromotionVersionDO row);

    @Insert(
            "INSERT INTO trade_promotion_scope(root_id,version,type,target_id)"
                    + " VALUES(#{root},#{version},#{type},#{target})")
    int insertScope(
            @Param("root") long root,
            @Param("version") int version,
            @Param("type") String type,
            @Param("target") long target);

    @Select("SELECT * FROM trade_promotion_version WHERE root_id=#{root} AND version=#{version}")
    PromotionVersionDO version(@Param("root") long root, @Param("version") int version);

    @Select("SELECT * FROM trade_promotion_counter WHERE root_id=#{id} FOR UPDATE")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    PromotionCounterDO lockCounter(long id);

    @Select(
            "SELECT held_count+used_count FROM trade_promotion_member_counter WHERE root_id=#{id}"
                    + " ORDER BY held_count+used_count DESC,member_id LIMIT 1 FOR UPDATE")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    Long largestMemberUsage(long id);

    @Update(
            "UPDATE trade_promotion_counter SET global_limit=#{global},member_limit=#{member} WHERE"
                    + " root_id=#{root}")
    int setLimits(
            @Param("root") long root, @Param("global") Long global, @Param("member") Long member);

    @Insert(
            "INSERT INTO trade_promotion_member_counter(root_id,member_id)"
                    + " VALUES(#{root},#{member}) ON DUPLICATE KEY UPDATE member_id=member_id")
    int ensureMemberCounter(@Param("root") long root, @Param("member") long member);

    @Select(
            "SELECT * FROM trade_promotion_member_counter WHERE root_id=#{root} AND"
                    + " member_id=#{member} FOR UPDATE")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    PromotionCounterDO lockMemberCounter(@Param("root") long root, @Param("member") long member);

    @Update(
            "UPDATE trade_promotion_counter SET held_count=held_count+1 WHERE root_id=#{root} AND"
                    + " held_count<9223372036854775807-used_count AND (global_limit IS NULL OR"
                    + " held_count+used_count<global_limit)")
    int takeGlobal(long root);

    @Update(
            "UPDATE trade_promotion_member_counter SET held_count=held_count+1 WHERE"
                    + " root_id=#{root} AND member_id=#{member} AND"
                    + " held_count<9223372036854775807-used_count AND (#{limit} IS NULL OR"
                    + " held_count+used_count<#{limit})")
    int takeMember(
            @Param("root") long root, @Param("member") long member, @Param("limit") Long limit);

    @Select("SELECT order_no,member_id,status FROM trade_order WHERE order_no=#{order} FOR UPDATE")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    PromotionOrderDO lockOrder(String order);

    @Select(
            "SELECT * FROM trade_promotion_usage WHERE order_no=#{order} AND root_id=#{root} FOR"
                    + " UPDATE")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    PromotionUsageDO lockUsage(@Param("order") String order, @Param("root") long root);

    @Insert(
            "INSERT INTO"
                + " trade_promotion_usage(order_no,root_id,version,member_id,discount_fen,rule_hash,state,created_at_epoch_ms,updated_at_epoch_ms)"
                + " VALUES(#{row.orderNo},#{row.rootId},#{row.version},#{row.memberId},#{row.discountFen},#{row.ruleHash},'HELD',#{now},#{now})")
    int insertUsage(@Param("row") PromotionUsageDO row, @Param("now") long now);

    @Update(
            "UPDATE trade_promotion_counter SET held_count=held_count-1,used_count=used_count+CASE"
                + " WHEN #{target}='USED' THEN 1 ELSE 0 END WHERE root_id=#{root} AND held_count>0")
    int finishGlobal(@Param("root") long root, @Param("target") String target);

    @Update(
            "UPDATE trade_promotion_member_counter SET"
                + " held_count=held_count-1,used_count=used_count+CASE WHEN #{target}='USED' THEN 1"
                + " ELSE 0 END WHERE root_id=#{root} AND member_id=#{member} AND held_count>0")
    int finishMember(
            @Param("root") long root, @Param("member") long member, @Param("target") String target);

    @Update(
            "UPDATE trade_promotion_usage SET state=#{target},updated_at_epoch_ms=#{now} WHERE"
                    + " order_no=#{order} AND root_id=#{root} AND state='HELD'")
    int finishUsage(
            @Param("order") String order,
            @Param("root") long root,
            @Param("target") String target,
            @Param("now") long now);

    @Insert(
            "INSERT INTO"
                + " trade_promotion_usage_event(order_no,root_id,from_state,target_state,reason,occurred_at_epoch_ms)"
                + " VALUES(#{order},#{root},#{from},#{target},#{reason},#{now})")
    int event(
            @Param("order") String order,
            @Param("root") long root,
            @Param("from") String from,
            @Param("target") String target,
            @Param("reason") String reason,
            @Param("now") long now);
}
