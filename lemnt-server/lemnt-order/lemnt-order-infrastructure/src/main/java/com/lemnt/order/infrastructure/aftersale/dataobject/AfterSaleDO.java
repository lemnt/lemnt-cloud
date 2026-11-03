package com.lemnt.order.infrastructure.aftersale.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后表数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("order_after_sale")
public class AfterSaleDO extends BaseDO {

    /** 售后数值主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 唯一售后业务单号，兼容原支付退款关联。 */
    private String refundNo;

    /** 订单数值主键。 */
    private Long orderId;

    /** 原订单SKU及SPU快照，不查询当前商品补写。 */
    private Long skuId;

    private Long spuId;
    private String spuName;

    /** 原订单规格名称和值JSON。 */
    private String specs;

    private String picUrl;

    /** 实际退款成功时间。 */
    private LocalDateTime refundTime;

    /** 实际审批人员，历史无依据时为空。 */
    private Long auditUserId;

    /** 逻辑删除标记。 */
    @TableLogic(value = "0", delval = "1")
    private Boolean deleted;

    private String orderNo;
    private String requestNo;
    private Long memberId;
    private String paymentNo;
    private BigDecimal amount;
    private String reason;
    private String status;
    private String decisionReason;
    private LocalDateTime requestedAt;
    private LocalDateTime decidedAt;

    /** 正在退款的订单号。 */
    private String activeOrderNo;

    private Long orderItemId;
    private Integer quantity;
    private String type;
    private String returnCarrierCode;
    private String returnTrackingNo;
    private LocalDateTime returnShippedAt;
    private LocalDateTime returnReceivedAt;

    /** 第164批冻结结果；历史未记录的值保持为空。 */
    /** 本次商品优惠归因，单位元，不额外增加现金退款。 */
    private BigDecimal refundDiscountAmount;

    /** 本次运费优惠归因，单位元，不额外增加现金退款。 */
    private BigDecimal refundDeliveryDiscountPrice;

    /** 生成退单时冻结的应返使用积分，到账以账户流水为准。 */
    private Long returnUsePoint;

    /** 生成退单时冻结的应回收赠送积分。 */
    private Long deductGivePoint;

    /** 退单生成时的分摊规则版本，历史未记录为NULL。 */
    private Integer refundRuleVersion;
}
