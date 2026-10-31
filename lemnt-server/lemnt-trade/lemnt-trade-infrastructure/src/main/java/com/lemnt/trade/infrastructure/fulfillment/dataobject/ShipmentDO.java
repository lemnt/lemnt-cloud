package com.lemnt.trade.infrastructure.fulfillment.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 发货数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("trade_shipment")
public class ShipmentDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private String orderNo;
    private String requestNo;
    private String fingerprint;
    private String carrierCode;
    private String trackingNo;
    private String recipientName;
    private String phone;
    private String address;
    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;
}
