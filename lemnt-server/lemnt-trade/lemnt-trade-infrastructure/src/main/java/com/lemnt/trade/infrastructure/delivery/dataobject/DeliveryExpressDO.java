package com.lemnt.trade.infrastructure.delivery.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * trade_delivery_express 数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_delivery_express")
public class DeliveryExpressDO extends BaseDO {
    /**
     * 编号。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 快递公司编码。
     */
    private String code;

    /**
     * 快递公司名称。
     */
    private String name;

    /**
     * 图标地址。
     */
    @TableField(
            updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS)
    private String logo;

    /**
     * 排序。
     */
    private Integer sort;

    /**
     * 状态，0 启用、1 停用。
     */
    private Integer status;

    /**
     * 逻辑删除标记。
     */
    @TableLogic(value = "0", delval = "1")
    private Boolean deleted;
}
