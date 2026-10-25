package com.lemnt.mall.adapter.web.store.converter;

import com.lemnt.mall.application.store.dto.*;
import com.lemnt.mall.adapter.web.store.res.*;

public final class StoreResConverter {
    private StoreResConverter() {}

    public static StoreProductRes product(StoreProductDTO v) {
        return v == null
                ? null
                : new StoreProductRes(
                        v.id(),
                        v.name(),
                        v.categoryId(),
                        v.brandId(),
                        v.image(),
                        v.minPrice(),
                        v.maxPrice(),
                        v.available());
    }

    public static StoreSkuRes sku(StoreSkuDTO v) {
        return v == null
                ? null
                : new StoreSkuRes(
                        v.id(), v.name(), v.price(), v.stock(), v.image(), v.properties());
    }

    public static StoreCategoryRes category(StoreCategoryDTO v) {
        return v == null ? null : new StoreCategoryRes(v.id(), v.parentId(), v.name(), v.picUrl());
    }

    public static StoreDetailRes detail(StoreDetailDTO v) {
        return v == null
                ? null
                : new StoreDetailRes(
                        v.id(),
                        v.name(),
                        v.categoryId(),
                        v.brandId(),
                        v.description(),
                        v.image(),
                        v.skus().stream().map(StoreResConverter::sku).toList());
    }

    public static StorePageRes page(StorePageDTO v) {
        return v == null
                ? null
                : new StorePageRes(
                        v.records().stream().map(StoreResConverter::product).toList(), v.total());
    }
}
