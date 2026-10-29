package com.lemnt.trade.application.order.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.order.cmd.OrderCommentCreateCmd;
import com.lemnt.trade.domain.order.gateway.OrderCommentGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.trade.domain.order.model.OrderCommentState;
import com.lemnt.trade.domain.order.model.TradeOperator;
import com.lemnt.trade.domain.order.model.VerifiedOrderComment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.Objects;

/** 交易核验购买并编排评价，订单锁内完成行状态、汇总和日志。 */
@Component
@RequiredArgsConstructor
public class OrderCommentService {
    private final TradeOrderGateway orders;
    private final TradeOrderItemGateway items;
    private final OrderCommentGateway comments;
    private final OrderLogService logs;

    /** 远程已保存而本地失败时，原完整内容重试修复状态，不重复评价或日志。 */
    @Transactional(rollbackFor = Exception.class)
    public Long create(OrderCommentCreateCmd cmd) {
        require(cmd != null && cmd.memberId() != null && cmd.memberId() > 0, "请先登录", 401);
        require(cmd.orderNo() != null && cmd.orderNo().matches("[A-Za-z0-9_-]{1,40}")
                && cmd.skuId() != null && cmd.skuId() > 0
                && (cmd.orderItemId() == null || cmd.orderItemId() > 0), "订单或订单项参数不合法", 400);
        var order = orders.lockHeader(cmd.orderNo());
        require(order != null && cmd.memberId().equals(order.memberId()), "本人订单不存在", 404);
        var lines = items.items(order.id());
        var candidates = lines.stream().filter(line -> cmd.skuId().equals(line.productId())).toList();
        require(cmd.orderItemId() != null || candidates.size() == 1, "请使用真实订单项编号定位商品", 400);
        var line = candidates.stream().filter(value -> cmd.orderItemId() == null || cmd.orderItemId().equals(value.orderItemId()))
                .findFirst().orElse(null);
        require(line != null && line.orderItemId() != null, "订单项不属于当前订单或商品", 404);
        require(Boolean.TRUE.equals(line.commentStatus()) || "COMPLETED".equals(order.status()), "只能评价本人已完成订单内的商品", 400);
        var verified = new VerifiedOrderComment(order.memberId(), order.orderNo(), line.productId(), order.id(), line.orderItemId(),
                line.spuId(), line.spuName(), line.picUrl(), line.properties() == null ? null : line.properties().stream().map(property ->
                        new VerifiedOrderComment.Property(property.propertyId(), property.propertyName(), property.valueId(), property.valueName())).toList(),
                candidates.size() == 1, !Boolean.TRUE.equals(line.commentStatus()), cmd.score(), cmd.content(), cmd.descriptionScores(), cmd.benefitScores(), cmd.anonymous(), cmd.picUrls());
        // 商城私有保存不能回查交易，避免在订单锁内形成循环RPC。
        Long commentId = comments.saveVerified(verified);
        require(commentId != null && commentId > 0, "评价保存结果未确认", 503);
        if (!Boolean.TRUE.equals(line.commentStatus())) {
            require(items.markCommented(order.id(), line.orderItemId()), "订单项评价状态更新失败", 409);
        }
        var current = items.items(order.id());
        boolean all = !current.isEmpty() && current.stream().allMatch(value -> Boolean.TRUE.equals(value.commentStatus()));
        Boolean aggregate = all ? Boolean.TRUE : current.stream().anyMatch(value -> Boolean.FALSE.equals(value.commentStatus())) ? Boolean.FALSE : null;
        if (!Objects.equals(aggregate, order.commentStatus())) {
            var finishTime = all ? orders.databaseNow() : order.finishTime();
            require(orders.updateCommentState(new OrderCommentState(order.id(), aggregate, finishTime)), "订单评价状态更新失败", 409);
            if (all) {
                logs.record(order, order.status(), order.status(), 33, TradeOperator.member(order.memberId()), "用户完成订单评价");
            }
        }
        return commentId;
    }

    private void require(boolean value, String message, int code) {
        if (!value) {
            throw new ServiceException(message, code);
        }
    }
}
