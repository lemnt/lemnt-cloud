package com.lemnt.mall.infrastructure.product.converter;

import com.lemnt.mall.domain.product.model.ProductSpecValue;
import com.lemnt.mall.infrastructure.product.dataobject.ProductSpecValueDO;

/** 规格字典领域与持久化转换。 */
public final class ProductSpecValueConverter {
    private ProductSpecValueConverter() {
    }

    /** 转换领域记录。 */
    public static ProductSpecValue toModel(ProductSpecValueDO row) {
        return row == null ? null : new ProductSpecValue(row.getId(), row.getSpecId(), row.getName(),
            row.getRemark(), row.getCreateTime());
    }

    /** 转换数据对象。 */
    public static ProductSpecValueDO toDO(ProductSpecValue value) {
        return new ProductSpecValueDO().setId(value.id()).setSpecId(value.specId()).setName(value.name())
                .setRemark(value.remark());
    }
}
