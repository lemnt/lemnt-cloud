package com.lemnt.trade.infrastructure.config.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** trade_config 数据对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_config")
public class TradeConfigDO extends BaseDO {
    /** 单例配置编号。 */
    @TableId(type = IdType.INPUT)
    private Long id;

    /** 仅退款原因。 */
    private String afterSaleRefundReasons;

    /** 退货退款原因。 */
    private String afterSaleReturnReasons;

    /** 是否全场包邮。 */
    private Boolean deliveryExpressFreeEnabled;

    /** 全场包邮金额门槛，分。 */
    private Integer deliveryExpressFreePrice;

    /** 是否开启自提。 */
    private Boolean deliveryPickUpEnabled;

    /** 是否启用分佣。 */
    private Boolean brokerageEnabled;

    /** 分佣模式。 */
    private Integer brokerageEnabledCondition;

    /** 分销绑定模式。 */
    private Integer brokerageBindMode;

    /** 分销海报地址。 */
    private String brokeragePosterUrls;

    /** 一级返佣百分比。 */
    private Integer brokerageFirstPercent;

    /** 二级返佣百分比。 */
    private Integer brokerageSecondPercent;

    /** 最低提现金额，分。 */
    private Integer brokerageWithdrawMinPrice;

    /** 提现手续费百分比。 */
    private Integer brokerageWithdrawFeePercent;

    /** 佣金冻结天数。 */
    private Integer brokerageFrozenDays;

    /** 提现方式。 */
    private String brokerageWithdrawTypes;

    /** 逻辑删除标记。 */
    @TableLogic(value = "0", delval = "1")
    private Boolean deleted;
}
