package com.lemnt.trade.infrastructure.checkout.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface SettlementMapper {
    @Select(
            "SELECT order_no FROM trade_settlement_order WHERE member_id=#{memberId} AND"
                    + " settlement_id=#{id}")
    String orderNo(@Param("memberId") Long memberId, @Param("id") String id);

    @Insert(
            "INSERT INTO trade_settlement_order(settlement_id,member_id,order_no)"
                    + " VALUES(#{id},#{memberId},#{orderNo})")
    int bind(
            @Param("memberId") Long memberId,
            @Param("id") String id,
            @Param("orderNo") String orderNo);

    @Select(
            "SELECT COUNT(*) FROM trade_settlement_abandon WHERE member_id=#{memberId} AND"
                + " settlement_id=#{id}")
    int abandoned(@Param("memberId") Long memberId, @Param("id") String id);

    @Insert(
            "INSERT INTO trade_settlement_abandon(member_id,settlement_id)"
                + " VALUES(#{memberId},#{id}) ON DUPLICATE KEY UPDATE settlement_id=settlement_id")
    int abandon(@Param("memberId") Long memberId, @Param("id") String id);
}
