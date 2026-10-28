package com.lemnt.mall.infrastructure.product.dataobject;

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
@TableName("mall_product_sku_spec")
public class ProductSkuSpecDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long productId;
    /** 字典关联，旧行保持为空。 */
    private Long propertyId;
    private Long valueId;
    private String propertyName;
    private String propertyValue;
}
