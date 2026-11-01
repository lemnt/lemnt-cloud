package com.lemnt.trade.application.order.cmd;

import java.io.Serializable;

/** 后台修改未发货快递订单的收件快照。 */
public record OrderAdjustAddressCmd(String orderNo, String receiverName, String receiverMobile,
        Long receiverAreaId, String receiverDetailAddress, Long operatorId) implements Serializable {
}
