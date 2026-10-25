package com.lemnt.mall.domain.store.gateway;

import com.lemnt.mall.domain.store.model.*;
import com.lemnt.mall.domain.catalog.model.*;
import java.util.List;

public interface StoreCatalogGateway {
    List<Category> categories();

    List<StoreProduct> products(StoreFilter filter);

    long count(StoreFilter filter);

    CatalogSpu product(Long id);
}
