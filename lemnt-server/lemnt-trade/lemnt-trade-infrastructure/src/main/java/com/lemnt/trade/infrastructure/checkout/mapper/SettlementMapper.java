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
}
