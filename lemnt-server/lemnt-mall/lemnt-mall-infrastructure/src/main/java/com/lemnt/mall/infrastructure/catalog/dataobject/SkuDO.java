package com.lemnt.mall.infrastructure.catalog.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SKU数据对象。
 */
@TableName("mall_product")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class SkuDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spuId;
    @TableField("product_code")
    private String code;
    private String name;
    private BigDecimal price;
    private Integer stock;
    @TableField("main_image")
    private String image;
    private String status;
    private String variantKey;
}
