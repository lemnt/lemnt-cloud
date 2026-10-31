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

/** trade_pay_app 数据对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_pay_app")
public class PayAppDO extends BaseDO {
    /** 应用编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 应用标识，创建后不可修改。 */
    private String appKey;

    /** 应用名。 */
    private String name;

    /** 状态，0 启用、1 停用。 */
    private Integer status;

    /** 备注。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String remark;

    /** 支付结果回调地址。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String orderNotifyUrl;

    /** 退款结果回调地址。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String refundNotifyUrl;

    /** 逻辑删除标记。 */
    @TableLogic(value = "0", delval = "1")
    private Boolean deleted;
}
