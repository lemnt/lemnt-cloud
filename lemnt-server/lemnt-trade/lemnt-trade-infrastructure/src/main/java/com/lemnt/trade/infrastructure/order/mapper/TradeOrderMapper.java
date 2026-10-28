package com.lemnt.trade.infrastructure.order.mapper;

import com.lemnt.trade.domain.fulfillment.model.FulfillmentFilter;
import com.lemnt.trade.infrastructure.fulfillment.dataobject.OrderSummaryDO;
import com.lemnt.trade.infrastructure.order.dataobject.OrderTotalDO;
import com.lemnt.trade.infrastructure.order.dataobject.TradeOrderDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/** TradeOrder 表数据访问接口。 */
@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrderDO> {
    /** 聚合订单表，避免拉取全部订单到内存。 */
    @Select("<script>SELECT COUNT(*) AS order_count, COALESCE(SUM(total_amount),0) AS order_pay_price"
            + " FROM trade_order <if test='orderNo != null'>WHERE order_no=#{orderNo}</if></script>")
    OrderTotalDO total(@Param("orderNo") String orderNo);

    String Fulfillment_ORDER_FILTER = "<where><if test='orderNo != null'>o.order_no=#{orderNo}</if><if test='status !="
            + " null'>AND o.status=#{status}</if></where>";

    /**
     * 查询订单。
     */
    @Select(
            "<script>SELECT o.order_no,o.member_id,o.total_amount,o.status,o.create_time, CASE WHEN"
            + " c.order_no IS NULL THEN FALSE ELSE TRUE END AS inventory_confirmed,"
            + " EXISTS(SELECT 1 FROM trade_after_sale r WHERE r.order_no=o.order_no"
            + " AND r.status IN ('REQUESTED','AWAITING_RETURN','RETURNED','WAIT_REFUND','REFUNDING')) AS active_refund,"
            + " s.carrier_code,s.tracking_no,s.shipped_at,s.received_at FROM trade_order o LEFT"
            + " JOIN trade_inventory_confirmation c ON c.order_no=o.order_no LEFT JOIN"
            + " trade_shipment s ON s.order_no=o.order_no "
            + Fulfillment_ORDER_FILTER
            + " ORDER BY o.id DESC LIMIT #{limit} OFFSET #{offset}</script>")
    List<OrderSummaryDO> orders(FulfillmentFilter filter);

    /**
     * 订单数量。
     */
    @Select("<script>SELECT COUNT(*) FROM trade_order o " + Fulfillment_ORDER_FILTER + "</script>")
    long orderCount(FulfillmentFilter filter);

    /**
     * 数据库当前时间。
     */
    @Select("SELECT CURRENT_TIMESTAMP")
    LocalDateTime databaseNow();

    /**
     * 状态变更状态。
     */
    @Update(
            "UPDATE trade_order SET status=#{targetStatus}, update_time=CURRENT_TIMESTAMP "
            + "WHERE order_no=#{orderNo} AND status=#{expectedStatus}")
    int transitionStatus(
            @Param("orderNo") String orderNo,
            @Param("expectedStatus") String expectedStatus,
            @Param("targetStatus") String targetStatus);
}
