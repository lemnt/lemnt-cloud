package com.lemnt.mall.infrastructure.product.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 商品数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mall_product_sku")
public class ProductSkuDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属 SPU 编号。 */
    private Long spuId;
    /** SPU 内的规格组合键。 */
    private String variantKey;
    private String productCode;
    private String name;
    private String description;
    private String mainImage;
    private BigDecimal price;
    private Integer stock;
    private String status;
    private BigDecimal weight;
    private BigDecimal volume;
}
