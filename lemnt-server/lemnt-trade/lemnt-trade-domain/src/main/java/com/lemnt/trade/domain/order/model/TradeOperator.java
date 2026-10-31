package com.lemnt.trade.domain.order.model;

/** 操作发起方：系统0、会员1、后台2，来源于已验证上下文。 */
public record TradeOperator(Long userId, int userType) {
    public TradeOperator {
        if (userId == null || userId < 0 || userType < 0 || userType > 2
                || (userType == 0 ? userId != 0 : userId == 0)) {
            throw new IllegalArgumentException("操作用户不合法");
        }
    }
    /** 系统任务及自动回调。 */
    public static TradeOperator system() { return new TradeOperator(0L, 0); }
    /** 会员本人操作。 */
    public static TradeOperator member(Long id) { return new TradeOperator(id, 1); }
    /** 后台人员操作。 */
    public static TradeOperator administrator(Long id) { return new TradeOperator(id, 2); }
}
