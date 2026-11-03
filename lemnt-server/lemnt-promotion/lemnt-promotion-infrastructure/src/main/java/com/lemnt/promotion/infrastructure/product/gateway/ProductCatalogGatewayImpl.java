package com.lemnt.promotion.infrastructure.product.gateway;
import com.lemnt.product.client.ProductCatalogClient;
import com.lemnt.product.client.dto.ProductSnapshotDTO;
import com.lemnt.promotion.domain.product.gateway.ProductCatalogGateway;
import com.lemnt.promotion.domain.product.model.ProductSnapshot;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.*;
/** 当前目录只通过商品Client读取，不能访问商品表或回填历史快照。 */
@Component
public class ProductCatalogGatewayImpl implements ProductCatalogGateway {
    @DubboReference(check=false,timeout=5000,retries=0) private ProductCatalogClient client;
    @Override public ProductSnapshot findOnShelf(Long id) { return from(client.findOnShelf(id)); }
    @Override public Map<Long,ProductSnapshot> findOnShelfBatch(List<Long> ids) {
        if (ids==null || ids.isEmpty()) return Map.of();
        var result=new LinkedHashMap<Long,ProductSnapshot>();
        for(var item:client.findOnShelfBatch(ids)) result.put(item.id(),from(item));
        return result;
    }
    private ProductSnapshot from(ProductSnapshotDTO value) {
        return value==null?null:new ProductSnapshot(value.id(),value.productCode(),value.name(),value.price(),value.stock(),value.deliveryTypes(),value.deliveryTemplateId(),value.weight(),value.volume(),value.spuId(),value.spuName(),value.specs()==null?null:value.specs().stream().map(s->new ProductSnapshot.Spec(s.specId(),s.specName(),s.specValueId(),s.specValueName())).toList(),value.picUrl());
    }
}
