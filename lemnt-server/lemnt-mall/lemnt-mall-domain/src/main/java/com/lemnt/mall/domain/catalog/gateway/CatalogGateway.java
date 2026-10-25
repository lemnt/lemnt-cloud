package com.lemnt.mall.domain.catalog.gateway;

import com.lemnt.mall.domain.catalog.model.*;
import java.util.List;

public interface CatalogGateway {
    List<Category> categories();

    Category category(Long id);

    Category saveCategory(Category value);

    boolean deleteCategory(Long id);

    List<Brand> brands();

    Brand brand(Long id);

    Brand saveBrand(Brand value);

    boolean deleteBrand(Long id);

    CatalogSpu createSpu(CatalogSpu value);

    CatalogSpu spu(Long id);

    CatalogPage page(CatalogFilter filter);

    boolean updateSpu(CatalogSpu value);

    boolean shelf(Long id, boolean onShelf);

    CatalogSku sku(Long id);

    boolean updateSku(CatalogSku value);

    StockAdjustment adjustStock(StockAdjustment adjustment);
}
