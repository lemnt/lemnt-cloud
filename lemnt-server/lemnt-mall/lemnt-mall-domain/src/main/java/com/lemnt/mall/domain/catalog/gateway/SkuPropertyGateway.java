package com.lemnt.mall.domain.catalog.gateway;

import java.util.Map;

/** SkuProperty 表领域存储接口。 */
public interface SkuPropertyGateway {
    /** 读取单个 SKU 的属性。 */
    Map<String, String> properties(Long id);

    /** 批量读取商品所有 SKU 的属性。 */
    Map<Long, Map<String, String>> propertiesBySpu(Long id);

    /** 保存 SKU 的属性集合。 */
    void saveProperties(Long id, Map<String, String> values);
}
