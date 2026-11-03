package com.lemnt.trade.infrastructure.order.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 交易订单明细数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("trade_order_item")
public class TradeOrderItemDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Boolean commentStatus;

    /** 既有Java productId明确对应SKU，只存sku_id一列。 */
    @TableField("sku_id")
    private Long productId;

    /** 下单时SPU编号及名称。 */
    private Long spuId;

    private String spuName;

    /** 规格名称和值JSON，NULL表示历史未记录。 */
    private String specs;

    /** 下单时采用的商品图片。 */
    private String picUrl;

    private String productCode;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineAmount;

    /** 商品行实付，旧数据为空时保留旧分摊规则。 */
    private BigDecimal payAmount;

    private BigDecimal deliveryPrice;

    /** 第164批冻结结果；历史未记录的值保持为空。 */
    /** 商品优惠金额，单位元，历史未知为NULL。 */
    private BigDecimal discountAmount;

    /** 减免前运费，单位元，历史未知为NULL。 */
    private BigDecimal deliveryOriginalPrice;

    /** 运费优惠金额，单位元，历史未知为NULL。 */
    private BigDecimal deliveryDiscountPrice;

    /** 人工调价差额，正加负减，单位元，历史未知为NULL。 */
    private BigDecimal adjustmentAmount;

    /** 使用积分数量，历史未知为NULL。 */
    private Long usePoint;

    /** 赠送积分数量，历史未知为NULL。 */
    private Long givePoint;

    /** 已成功退款数量，首次售后成功同事务增加，历史未知为NULL。 */
    private Integer refundedQuantity;
}
