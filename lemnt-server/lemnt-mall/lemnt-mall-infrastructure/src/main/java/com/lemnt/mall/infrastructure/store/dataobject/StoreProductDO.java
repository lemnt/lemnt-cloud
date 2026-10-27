package com.lemnt.mall.infrastructure.store.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 门店商品数据对象。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class StoreProductDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Long categoryId;
    private Long brandId;
    private String image;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private boolean available;
}
