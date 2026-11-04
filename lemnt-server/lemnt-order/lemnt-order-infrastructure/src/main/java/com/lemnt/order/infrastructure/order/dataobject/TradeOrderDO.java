package com.lemnt.order.infrastructure.order.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易订单数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("order_info")
public class TradeOrderDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long memberId;
    private BigDecimal totalAmount;
    private String status;
    private String remark;
    private Boolean commentStatus;
    private LocalDateTime finishTime;
    private LocalDateTime receiveTime;

    /**
     * 使用积分数量，历史未知为NULL。
     */
    private Long usePoint;

    /**
     * 赠送积分数量，历史未知为NULL。
     */
    private Long givePoint;

    private Long addressId;
    private Integer deliveryType;
    private String receiverName;
    private String receiverMobile;
    private Long receiverAreaId;
    private String receiverDetailAddress;
    private Long pickUpStoreId;
    private String pickUpVerifyCode;
    private BigDecimal deliveryPrice;

    /**
     * 营销满减赠券模板及数量 JSON。
     */
    private String giveCouponTemplateCounts;

    /** 第164批冻结结果；历史未记录的值保持为空。 */
    /**
     * 商品原价合计，单位元，历史未知为NULL。
     */
    private BigDecimal originalAmount;

    /**
     * 商品优惠金额，单位元，历史未知为NULL。
     */
    private BigDecimal discountAmount;

    /**
     * 减免前运费，单位元，历史未知为NULL。
     */
    private BigDecimal deliveryOriginalPrice;

    /**
     * 运费优惠金额，单位元，历史未知为NULL。
     */
    private BigDecimal deliveryDiscountPrice;

    /**
     * 人工调价差额，正加负减，单位元，历史未知为NULL。
     */
    private BigDecimal adjustmentAmount;

    /**
     * 金额快照完整性：COMPLETE或LEGACY_PARTIAL。
     */
    private String pricingSnapshotStatus;

    /**
     * 支付前调价时递增的金额快照版本。
     */
    private Integer pricingSnapshotVersion;
}
