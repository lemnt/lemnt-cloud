package com.lemnt.mall.infrastructure.product.converter;

import com.lemnt.mall.domain.product.model.ProductSpec;
import com.lemnt.mall.infrastructure.product.dataobject.ProductSpecDO;

/** 规格字典领域与持久化转换。 */
public final class ProductSpecConverter {
    private ProductSpecConverter() {
    }

    /** 转换领域记录。 */
    public static ProductSpec toModel(ProductSpecDO row) {
        return row == null ? null : new ProductSpec(row.getId(), row.getName(), row.getRemark());
    }

    /** 转换数据对象。 */
    public static ProductSpecDO toDO(ProductSpec value) {
        return new ProductSpecDO().setId(value.id()).setName(value.name()).setRemark(value.remark());
    }
}
