package com.lemnt.mall.infrastructure.promotion.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 计价事实数据对象。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class PricingFactsDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long skuId;
    private Long spuId;
    private String productCode;
    private String name;
    private BigDecimal price;
    private Integer stock;
}
