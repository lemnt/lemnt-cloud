package com.lemnt.trade.domain.order.model;
/** 订单评价汇总状态；历史未知可以为null，完成时间只在全部确认时写入。 */
public record OrderCommentState(Long orderId, Boolean commentStatus, java.time.LocalDateTime finishTime) {}
