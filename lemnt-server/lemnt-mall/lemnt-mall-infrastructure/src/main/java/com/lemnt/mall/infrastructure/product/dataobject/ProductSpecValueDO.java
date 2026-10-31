package com.lemnt.mall.infrastructure.product.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/** 商品规格字典持久化对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mall_product_spec_value")
public class ProductSpecValueDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long specId;
    private String name;
    private String remark;
}
