package com.lemnt.trade.start;
import com.lemnt.trade.application.order.service.OrderCommentService;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Duration;
/** 到期评价调度，沿用项目Spring调度器，逐单事务且隔离失败。 */
@Component
@ConditionalOnProperty(name="lemnt.trade.auto-comment.enabled", havingValue="true")
public class TradeOrderAutoCommentScheduler {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TradeOrderAutoCommentScheduler.class);
    private final TradeOrderGateway orders;
    private final OrderCommentService comments;
    private final Duration expiry;
    public TradeOrderAutoCommentScheduler(TradeOrderGateway orders, OrderCommentService comments,
            @Value("${lemnt.trade.auto-comment.expire:7d}") Duration expiry) {
        if (expiry == null || expiry.isNegative() || expiry.isZero()) { throw new IllegalArgumentException("自动评价期限必须为正"); }
        this.orders=orders; this.comments=comments; this.expiry=expiry;
    }
    /** 使用数据库时间，不以应用节点时间推断订单是否超期。 */
    @Scheduled(fixedDelayString="${lemnt.trade.auto-comment.delay-ms:60000}")
    public void execute() {
        var cutoff=orders.databaseNow().minus(expiry);
        for (var no : orders.commentExpired(cutoff, 50)) {
            try { comments.createSystem(no, cutoff); }
            catch (RuntimeException error) { LOG.error("系统评价失败，订单 {}", no, error); }
        }
    }
}
