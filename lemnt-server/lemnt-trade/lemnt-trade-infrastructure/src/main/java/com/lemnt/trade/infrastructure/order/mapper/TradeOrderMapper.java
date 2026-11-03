package com.lemnt.trade.infrastructure.order.mapper;

import com.lemnt.trade.domain.fulfillment.model.FulfillmentFilter;
import com.lemnt.trade.infrastructure.fulfillment.dataobject.OrderSummaryDO;
import com.lemnt.trade.infrastructure.order.dataobject.OrderStatusCountDO;
import com.lemnt.trade.infrastructure.order.dataobject.OrderTotalDO;
import com.lemnt.trade.infrastructure.order.dataobject.TradeOrderDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/** TradeOrder 表数据访问接口。 */
@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrderDO> {
    /** 聚合订单表，避免拉取全部订单到内存。 */
    OrderTotalDO total(@Param("orderNo") String orderNo);

    /**
     * 查询订单。
     */
    List<OrderSummaryDO> orders(FulfillmentFilter filter);

    /**
     * 订单数量。
     */
    long orderCount(FulfillmentFilter filter);

    /** 会员本人订单按状态与评价状态分组计数。 */
    List<OrderStatusCountDO> memberStatusCounts(@Param("memberId") Long memberId);

    /**
     * 数据库当前时间。
     */
    LocalDateTime databaseNow();

    /**
     * 状态变更状态。
     */
    int transitionStatus(
            @Param("orderNo") String orderNo,
            @Param("expectedStatus") String expectedStatus,
            @Param("targetStatus") String targetStatus);

    /** 订单写操作共用的真实数据库行锁。 */
    TradeOrderDO lockByOrderNo(
            @Param("orderNo") String orderNo);
}
