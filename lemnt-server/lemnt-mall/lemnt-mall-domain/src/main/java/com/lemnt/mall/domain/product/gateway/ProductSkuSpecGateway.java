package com.lemnt.mall.domain.product.gateway;

import java.util.List;
import java.util.Map;

/** mall_product_sku_spec 表（SKU规格快照）领域存储接口。 */
public interface ProductSkuSpecGateway {
    /** 按SKU编号批量读取规格快照。 */
    Map<Long, Map<String, String>> propertiesBySkuIds(List<Long> ids);

    /** 检查属性名或特定值是否已用于SKU快照。 */
    boolean referenced(String name, String value);

    /** 读取单个 SKU 的属性。 */
    Map<String, String> properties(Long id);

    /** 批量读取商品所有 SKU 的属性。 */
    Map<Long, Map<String, String>> propertiesBySpu(Long id);

    /** 保存 SKU 的属性集合。 */
    void saveProperties(Long id, Map<String, String> values);
}
