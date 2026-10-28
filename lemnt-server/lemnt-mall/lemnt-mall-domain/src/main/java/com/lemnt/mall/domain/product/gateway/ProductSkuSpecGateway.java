package com.lemnt.mall.domain.product.gateway;

import com.lemnt.mall.domain.product.model.ProductSkuSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** mall_product_sku_spec表领域存储接口。 */
public interface ProductSkuSpecGateway {
    /** 一次读取SKU名值及编号快照。 */
    Map<Long, List<ProductSkuSpec>> specsBySkuIds(List<Long> ids);

    /** 按SPU批量读取其SKU规格行。 */
    Map<Long, List<ProductSkuSpec>> specsBySpu(Long id);

    /** 结算的名值视图来自同一规格快照，不另查字典。 */
    default Map<Long, Map<String, String>> propertiesBySkuIds(List<Long> ids) {
        var result = new HashMap<Long, Map<String, String>>();
        specsBySkuIds(ids).forEach((id, values) -> result.put(id, ProductSkuSpec.names(values)));
        return result;
    }

    /** 旧名值调用读取原规格快照。 */
    default Map<String, String> properties(Long id) {
        return ProductSkuSpec.names(specsBySkuIds(List.of(id)).getOrDefault(id, List.of()));
    }

    /** 兼容旧名值写入，不推断编号。 */
    void saveProperties(Long id, Map<String, String> values);

    /** 保存已校验的字典关联与名称快照。 */
    void saveSpecs(Long id, List<ProductSkuSpec> values);

    /** 优先按编号检查，未关联旧记录仍按原名值保护。 */
    boolean referenced(Long propertyId, Long valueId, String name, String value);
}
