package com.lemnt.trade.infrastructure.order.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 交易订单数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("trade_order")
public class TradeOrderDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long memberId;
    private BigDecimal totalAmount;
    private String status;
    private String remark;
    private Long usePoint;
    private Long givePoint;
    private Long addressId;
    private Integer deliveryType;
    private String receiverName;
    private String receiverMobile;
    private Long receiverAreaId;
    private String receiverDetailAddress;
    private BigDecimal deliveryPrice;
}
