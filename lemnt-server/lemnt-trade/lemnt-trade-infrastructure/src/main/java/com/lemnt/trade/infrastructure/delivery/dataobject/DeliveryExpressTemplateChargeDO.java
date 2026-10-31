package com.lemnt.trade.infrastructure.delivery.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * trade_delivery_express_template_charge 数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_delivery_express_template_charge")
public class DeliveryExpressTemplateChargeDO extends BaseDO {
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
     * 模板计费方式快照。
     */
    private Integer chargeMode;

    /**
     * 首量。
     */
    private BigDecimal startCount;

    /**
     * 首价，分。
     */
    private Integer startPrice;

    /**
     * 续量。
     */
    private BigDecimal extraCount;

    /**
     * 续价，分。
     */
    private Integer extraPrice;

    /**
     * 逻辑删除标记。
     */
    @TableLogic(value = "0", delval = "1")
    private Boolean deleted;
}
