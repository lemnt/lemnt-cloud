package com.lemnt.trade.application.pickup.cmd;

import java.io.Serializable;

/** 将已支付订单设置为自提命令。 */
public record PickUpAssignCmd(String orderNo, Long storeId, Long operatorId)
        implements Serializable {}
