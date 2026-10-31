package com.lemnt.trade.infrastructure.payment.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** trade_pay_channel 数据对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_pay_channel")
public class PayChannelConfigDO extends BaseDO {
    /** 渠道配置编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属应用编号。 */
    private Long appId;

    /** 渠道编码，创建后不可修改。 */
    private String code;

    /** 状态，0 启用、1 停用。 */
    private Integer status;

    /** 渠道费率，百分比，0-100。 */
    private BigDecimal feeRate;

    /** 备注。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String remark;

    /** 渠道配置 JSON 文本。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String config;

    /** 逻辑删除标记。 */
    @TableLogic(value = "0", delval = "1")
    private Boolean deleted;
}
