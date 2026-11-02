package com.lemnt.mall.infrastructure.product;

import com.lemnt.mall.infrastructure.product.converter.ProductCatalogConverter;
import com.lemnt.mall.domain.product.model.ProductSkuSpec;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** 当前SKU规格兼容读取旧四字段；不补造未知编号。 */
class SpecSnapshotCompatibilityTest {
    @Test
    void readsLegacyFieldsAndNameMapAsSpecs() {
        assertEquals(List.of(new ProductSkuSpec(3L, "颜色", 7L, "黑色")), ProductCatalogConverter.specs(
                "[{\"propertyId\":3,\"propertyName\":\"颜色\",\"valueId\":7,\"valueName\":\"黑色\"}]"));
        assertEquals(List.of(new ProductSkuSpec(null, "颜色", null, "黑色")),
                ProductCatalogConverter.specs("{\"颜色\":\"黑色\"}"));
        assertEquals(List.of(), ProductCatalogConverter.specs(null));
    }
}
