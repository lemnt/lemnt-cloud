package com.lemnt.trade.infrastructure.delivery.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * trade_delivery_express_template_free 数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_delivery_express_template_free")
public class DeliveryExpressTemplateFreeDO extends BaseDO {
    /**
     * 编号。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板编号。
     */
    private Long templateId;

    /**
     * 配送区域编号集合。
     */
    private String areaIds;

    /**
     * 包邮金额门槛，分。
     */
    private Integer freePrice;

    /**
     * 包邮计费量门槛。
     */
    private Integer freeCount;

    /**
     * 逻辑删除标记。
     */
    @TableLogic(value = "0", delval = "1")
    private Boolean deleted;
}
