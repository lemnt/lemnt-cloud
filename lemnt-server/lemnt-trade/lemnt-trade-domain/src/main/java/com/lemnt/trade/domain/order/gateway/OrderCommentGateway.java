package com.lemnt.trade.domain.order.gateway;
import com.lemnt.trade.domain.order.model.VerifiedOrderComment;
/** 评价数据归商城；交易只编排真实订单项状态。 */
public interface OrderCommentGateway {
    /** 同步保存已验证购买的评价。 */
    Long saveVerified(VerifiedOrderComment value);
}
