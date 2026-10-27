package com.lemnt.mall.infrastructure.checkout.mapper;

import com.lemnt.mall.infrastructure.checkout.dataobject.SettlementOrderDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** SettlementOrder 表数据访问接口。 */
@Mapper
public interface SettlementOrderMapper extends BaseMapper<SettlementOrderDO> {
    /**
     * 订单编号。
     */
    @Select(
            "SELECT order_no FROM mall_settlement_order WHERE member_id=#{memberId} AND"
            + " settlement_id=#{id}")
    String orderNo(@Param("memberId") Long memberId, @Param("id") String id);

    /**
     * 绑定。
     */
    @Insert(
            "INSERT INTO mall_settlement_order(settlement_id,member_id,order_no)"
            + " VALUES(#{id},#{memberId},#{orderNo})")
    int bind(
            @Param("memberId") Long memberId,
            @Param("id") String id,
            @Param("orderNo") String orderNo);
}
