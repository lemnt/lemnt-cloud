package com.lemnt.product.application.product.converter;

import com.lemnt.product.application.product.dto.ProductSpecDTO;
import com.lemnt.product.application.product.dto.ProductSpecValueDTO;
import com.lemnt.product.domain.product.model.ProductSpec;
import com.lemnt.product.domain.product.model.ProductSpecValue;

/** 规格字典应用输出转换。 */
public final class ProductSpecDTOConverter {
    private ProductSpecDTOConverter() {}

    /** 转换规格项。 */
    public static ProductSpecDTO property(ProductSpec value) {
        return value == null
                ? null
                : new ProductSpecDTO(value.id(), value.name(), value.remark(), value.createTime());
    }

    /** 转换规格值。 */
    public static ProductSpecValueDTO value(ProductSpecValue value) {
        return value == null
                ? null
                : new ProductSpecValueDTO(
                        value.id(),
                        value.specId(),
                        value.name(),
                        value.remark(),
                        value.createTime());
    }
}
