package com.lemnt.trade.infrastructure.fulfillment.mapper;

import com.lemnt.trade.domain.fulfillment.model.FulfillmentFilter;
import com.lemnt.trade.infrastructure.fulfillment.dataobject.RefundRequestDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/** RefundRequest 表数据访问接口。 */
@Mapper
public interface RefundRequestMapper extends BaseMapper<RefundRequestDO> {

    /**
     * 有效退款。
     */
    default int activeRefund(String orderNo) {
        return this.selectCount(Wrappers.<RefundRequestDO>lambdaQuery()
                .eq(RefundRequestDO::getActiveOrderNo, orderNo)
                .eq(RefundRequestDO::getStatus, "REQUESTED")).intValue();
    }

    /**
     * 查询退款。
     */
    default List<RefundRequestDO> refunds(FulfillmentFilter filter) {
        return this.selectPage(
                new Page<>((long) filter.offset() / filter.limit() + 1, filter.limit(), false),
                Wrappers.<RefundRequestDO>lambdaQuery()
                .eq(filter.orderNo() != null, RefundRequestDO::getOrderNo, filter.orderNo())
                .eq(filter.status() != null, RefundRequestDO::getStatus, filter.status())
                .orderByDesc(RefundRequestDO::getRequestedAt)
                .orderByAsc(RefundRequestDO::getRefundNo)).getRecords();
    }

    /**
     * 退款数量。
     */
    default long refundCount(FulfillmentFilter filter) {
        return this.selectCount(Wrappers.<RefundRequestDO>lambdaQuery()
                .eq(filter.orderNo() != null, RefundRequestDO::getOrderNo, filter.orderNo())
                .eq(filter.status() != null, RefundRequestDO::getStatus, filter.status()));
    }

    /**
     * 退款。
     */
    default RefundRequestDO refund(String refundNo) {
        return this.selectOne(Wrappers.<RefundRequestDO>lambdaQuery()
                .eq(RefundRequestDO::getRefundNo, refundNo));
    }

    /**
     * 锁定退款。
     */
    @Select("SELECT * FROM trade_refund_request WHERE refund_no=#{refundNo} FOR UPDATE")
    RefundRequestDO lockRefund(String refundNo);

    /**
     * 查找请求。
     */
    default RefundRequestDO findRequest(
            String orderNo, String requestNo) {
        return this.selectOne(Wrappers.<RefundRequestDO>lambdaQuery()
                .eq(RefundRequestDO::getOrderNo, orderNo)
                .eq(RefundRequestDO::getRequestNo, requestNo));
    }

    /**
     * 插入退款。
     */
    @Insert(
            "INSERT INTO"
            + " trade_refund_request(refund_no,order_no,request_no,member_id,payment_no,amount,reason,status,active_order_no,requested_at)"
            + " VALUES(#{refundNo},#{orderNo},#{requestNo},#{memberId},#{paymentNo},#{amount},#{reason},'REQUESTED',#{orderNo},#{requestedAt})")
    int insertRefund(RefundRequestDO row);

    /**
     * 拒绝。
     */
    default int reject(
            String refundNo,
            String reason,
            LocalDateTime time) {
        return this.update(null, Wrappers.<RefundRequestDO>lambdaUpdate()
                .set(RefundRequestDO::getStatus, "REJECTED")
                .set(RefundRequestDO::getActiveOrderNo, null)
                .set(RefundRequestDO::getDecisionReason, reason)
                .set(RefundRequestDO::getDecidedAt, time)
                .eq(RefundRequestDO::getRefundNo, refundNo)
                .eq(RefundRequestDO::getStatus, "REQUESTED"));
    }
}
