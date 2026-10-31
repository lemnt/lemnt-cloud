package com.lemnt.trade.infrastructure.delivery.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** trade_delivery_express_template 数据对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_delivery_express_template")
public class DeliveryExpressTemplateDO extends BaseDO {
    /** 编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模板名称。 */
    private String name;

    /** 计费方式，1 件数、2 重量千克、3 体积立方米。 */
    private Integer chargeMode;

    /** 排序。 */
    private Integer sort;

    /** 逻辑删除标记。 */
    @TableLogic(value = "0", delval = "1")
    private Boolean deleted;
}
