package com.lemnt.mall.infrastructure.catalog.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品SKU属性数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_sku_property")
public class SkuPropertyDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long productId;
    private String propertyName;
    private String propertyValue;
}
