package com.lemnt.pay.domain.payment.model;

/** 操作发起方：系统0、会员1、后台2，来源于已验证上下文。 */
public record PaymentOperator(Long userId, int userType) {
    public PaymentOperator {
        if (userId == null
                || userId < 0
                || userType < 0
                || userType > 2
                || (userType == 0 ? userId != 0 : userId == 0)) {
            throw new IllegalArgumentException("操作用户不合法");
        }
    }

    /** 系统任务及自动回调。 */
    public static PaymentOperator system() {
        return new PaymentOperator(0L, 0);
    }

    /** 会员本人操作。 */
    public static PaymentOperator member(Long id) {
        return new PaymentOperator(id, 1);
    }

    /** 后台人员操作。 */
    public static PaymentOperator administrator(Long id) {
        return new PaymentOperator(id, 2);
    }
}
