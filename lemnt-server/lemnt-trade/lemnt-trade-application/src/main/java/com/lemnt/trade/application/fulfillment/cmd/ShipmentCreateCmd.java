package com.lemnt.trade.application.fulfillment.cmd;

import com.lemnt.trade.domain.order.model.TradeOperator;
import java.io.Serializable;


/**
 * 发货创建命令。
 */
public record ShipmentCreateCmd(
        String orderNo,
        String requestNo,
        String carrierCode,
        String trackingNo,
        String recipientName,
        String phone,
        String address,
        TradeOperator operator
) implements Serializable {
    /** 内部任务及存量调用明确记为系统操作者。 */
    public ShipmentCreateCmd(String orderNo, String requestNo, String carrierCode, String trackingNo,
            String recipientName, String phone, String address) {
        this(orderNo, requestNo, carrierCode, trackingNo, recipientName, phone, address, TradeOperator.system());
    }
}
