package com.lemnt.trade.infrastructure.fulfillment.mapper;

import com.lemnt.trade.infrastructure.fulfillment.dataobject.*;
import com.lemnt.trade.domain.fulfillment.model.FulfillmentFilter;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.time.LocalDateTime;

@Mapper
public interface FulfillmentMapper {
    @Select("SELECT COUNT(*) FROM trade_inventory_confirmation WHERE order_no=#{orderNo}")
    int confirmed(String orderNo);

    @Insert(
            "INSERT INTO trade_inventory_confirmation(order_no,confirmed_at)"
                    + " VALUES(#{orderNo},#{time}) ON DUPLICATE KEY UPDATE order_no=order_no")
    int confirm(@Param("orderNo") String orderNo, @Param("time") LocalDateTime time);

    @Select(
            "SELECT COUNT(*) FROM trade_refund_request WHERE active_order_no=#{orderNo} AND"
                    + " status='REQUESTED'")
    int activeRefund(String orderNo);

    @Select("SELECT * FROM trade_shipment WHERE order_no=#{orderNo}")
    ShipmentDO shipment(String orderNo);

    @Insert(
            "INSERT INTO"
                + " trade_shipment(order_no,request_no,fingerprint,carrier_code,tracking_no,recipient_name,phone,address,shipped_at)"
                + " VALUES(#{orderNo},#{requestNo},#{fingerprint},#{carrierCode},#{trackingNo},#{recipientName},#{phone},#{address},#{shippedAt})")
    int insertShipment(ShipmentDO row);

    @Update(
            "UPDATE trade_shipment SET received_at=#{time} WHERE order_no=#{orderNo} AND"
                    + " received_at IS NULL")
    int receive(@Param("orderNo") String orderNo, @Param("time") LocalDateTime time);

    String ORDER_FILTER =
            "<where><if test='orderNo != null'>o.order_no=#{orderNo}</if><if test='status !="
                    + " null'>AND o.status=#{status}</if></where>";

    @Select(
            "<script>SELECT o.order_no,o.member_id,o.total_amount,o.status,o.create_time, CASE WHEN"
                + " c.order_no IS NULL THEN FALSE ELSE TRUE END AS inventory_confirmed,"
                + " EXISTS(SELECT 1 FROM trade_refund_request r WHERE r.active_order_no=o.order_no"
                + " AND r.status='REQUESTED') AS active_refund,"
                + " s.carrier_code,s.tracking_no,s.shipped_at,s.received_at FROM trade_order o LEFT"
                + " JOIN trade_inventory_confirmation c ON c.order_no=o.order_no LEFT JOIN"
                + " trade_shipment s ON s.order_no=o.order_no "
                    + ORDER_FILTER
                    + " ORDER BY o.id DESC LIMIT #{limit} OFFSET #{offset}</script>")
    List<OrderSummaryDO> orders(FulfillmentFilter filter);

    @Select("<script>SELECT COUNT(*) FROM trade_order o " + ORDER_FILTER + "</script>")
    long orderCount(FulfillmentFilter filter);

    String REFUND_FILTER =
            "<where><if test='orderNo != null'>order_no=#{orderNo}</if><if test='status !="
                    + " null'>AND status=#{status}</if></where>";

    @Select(
            "<script>SELECT * FROM trade_refund_request "
                    + REFUND_FILTER
                    + " ORDER BY requested_at DESC,refund_no LIMIT #{limit} OFFSET"
                    + " #{offset}</script>")
    List<RefundRequestDO> refunds(FulfillmentFilter filter);

    @Select("<script>SELECT COUNT(*) FROM trade_refund_request " + REFUND_FILTER + "</script>")
    long refundCount(FulfillmentFilter filter);

    @Select("SELECT * FROM trade_refund_request WHERE refund_no=#{refundNo}")
    RefundRequestDO refund(String refundNo);

    @Select("SELECT * FROM trade_refund_request WHERE refund_no=#{refundNo} FOR UPDATE")
    RefundRequestDO lockRefund(String refundNo);

    @Select(
            "SELECT * FROM trade_refund_request WHERE order_no=#{orderNo} AND"
                    + " request_no=#{requestNo}")
    RefundRequestDO findRequest(
            @Param("orderNo") String orderNo, @Param("requestNo") String requestNo);

    @Insert(
            "INSERT INTO"
                + " trade_refund_request(refund_no,order_no,request_no,member_id,payment_no,amount,reason,status,active_order_no,requested_at)"
                + " VALUES(#{refundNo},#{orderNo},#{requestNo},#{memberId},#{paymentNo},#{amount},#{reason},'REQUESTED',#{orderNo},#{requestedAt})")
    int insertRefund(RefundRequestDO row);

    @Update(
            "UPDATE trade_refund_request SET"
                + " status='REJECTED',active_order_no=NULL,decision_reason=#{reason},decided_at=#{time}"
                + " WHERE refund_no=#{refundNo} AND status='REQUESTED'")
    int reject(
            @Param("refundNo") String refundNo,
            @Param("reason") String reason,
            @Param("time") LocalDateTime time);
}
