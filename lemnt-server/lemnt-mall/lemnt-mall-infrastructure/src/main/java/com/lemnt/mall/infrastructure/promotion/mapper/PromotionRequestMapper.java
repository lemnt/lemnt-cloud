package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionRequestDO;
import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionRootDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** PromotionRequest 表数据访问接口。 */
@Mapper
public interface PromotionRequestMapper extends BaseMapper<PromotionRequestDO> {
    /**
     * 确保请求。
     */
    @Insert(
            "INSERT INTO"
            + " mall_promotion_admin_request(operator_id,request_no,action,fingerprint,created_at_epoch_ms)"
            + " VALUES(#{operator},#{request},#{action},#{fingerprint},UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000.0)"
            + " ON DUPLICATE KEY UPDATE request_no=request_no")
    int ensureRequest(
            @Param("operator") long operator,
            @Param("request") String request,
            @Param("action") String action,
            @Param("fingerprint") String fingerprint);

    /**
     * 锁定请求。
     */
    @Select(
            "SELECT * FROM mall_promotion_admin_request WHERE operator_id=#{operator} AND"
            + " request_no=#{request} FOR UPDATE")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    PromotionRequestDO lockRequest(
            @Param("operator") long operator, @Param("request") String request);

    /**
     * 完成请求。
     */
    @Update(
            "UPDATE mall_promotion_admin_request SET"
            + " completed_at_epoch_ms=UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000.0,result_id=#{receipt.id},result_revision=#{receipt.revision},result_state=#{receipt.state},result_active_version=#{receipt.activeVersion},result_last_version=#{receipt.lastVersion}"
            + " WHERE operator_id=#{operator} AND request_no=#{request} AND result_id IS NULL")
    int completeRequest(
            @Param("operator") long operator,
            @Param("request") String request,
            @Param("receipt") PromotionRootDO receipt);
}
