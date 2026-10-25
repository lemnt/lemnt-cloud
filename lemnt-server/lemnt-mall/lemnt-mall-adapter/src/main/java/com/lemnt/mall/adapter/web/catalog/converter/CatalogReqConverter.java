package com.lemnt.mall.adapter.web.catalog.converter;

import com.lemnt.mall.adapter.web.catalog.req.*;
import com.lemnt.mall.application.catalog.cmd.*;
import com.lemnt.mall.application.catalog.qry.SpuPageQry;

public final class CatalogReqConverter {
    private CatalogReqConverter() {}

    public static CategorySaveCmd category(Long id, CategorySaveReq req) {
        return new CategorySaveCmd(
                id, req.parentId(), req.name(), req.picUrl(), req.sort(), req.status());
    }

    public static BrandSaveCmd brand(Long id, BrandSaveReq req) {
        return new BrandSaveCmd(
                id, req.name(), req.picUrl(), req.description(), req.sort(), req.status());
    }

    public static SpuCreateCmd create(SpuCreateReq req) {
        return new SpuCreateCmd(
                req.code(),
                req.name(),
                req.categoryId(),
                req.brandId(),
                req.description(),
                req.image(),
                req.skus().stream()
                        .map(
                                s ->
                                        new SpuCreateCmd.SkuDraft(
                                                s.code(),
                                                s.name(),
                                                s.price(),
                                                s.stock(),
                                                s.image(),
                                                s.properties()))
                        .toList());
    }

    public static SpuUpdateCmd update(Long id, SpuUpdateReq req) {
        return new SpuUpdateCmd(
                id, req.name(), req.categoryId(), req.brandId(), req.description(), req.image());
    }

    public static SpuPageQry page(SpuPageReq req) {
        return new SpuPageQry(req.name(), req.status(), req.pageNum(), req.pageSize());
    }

    public static SpuShelfCmd shelf(Long id, SpuShelfReq req) {
        return new SpuShelfCmd(id, req.onShelf());
    }

    public static SkuUpdateCmd sku(Long id, SkuUpdateReq req) {
        return new SkuUpdateCmd(id, req.name(), req.price(), req.image());
    }

    public static StockAdjustCmd stock(Long id, StockAdjustReq req) {
        return new StockAdjustCmd(id, req.requestNo(), req.delta(), req.reason());
    }
}
