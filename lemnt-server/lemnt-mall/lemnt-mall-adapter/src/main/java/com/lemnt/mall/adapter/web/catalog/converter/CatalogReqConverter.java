package com.lemnt.mall.adapter.web.catalog.converter;

import com.lemnt.mall.adapter.web.catalog.req.BrandSaveReq;
import com.lemnt.mall.adapter.web.catalog.req.CategorySaveReq;
import com.lemnt.mall.adapter.web.catalog.req.MallBrandCreateReq;
import com.lemnt.mall.adapter.web.catalog.req.MallBrandUpdateReq;
import com.lemnt.mall.adapter.web.catalog.req.MallCategoryCreateReq;
import com.lemnt.mall.adapter.web.catalog.req.MallCategoryUpdateReq;
import com.lemnt.mall.adapter.web.catalog.req.SkuUpdateReq;
import com.lemnt.mall.adapter.web.catalog.req.SpuCreateReq;
import com.lemnt.mall.adapter.web.catalog.req.SpuPageReq;
import com.lemnt.mall.adapter.web.catalog.req.SpuShelfReq;
import com.lemnt.mall.adapter.web.catalog.req.SpuUpdateReq;
import com.lemnt.mall.adapter.web.catalog.req.StockAdjustReq;
import com.lemnt.mall.application.catalog.cmd.BrandSaveCmd;
import com.lemnt.mall.application.catalog.cmd.CategorySaveCmd;
import com.lemnt.mall.application.catalog.cmd.SkuUpdateCmd;
import com.lemnt.mall.application.catalog.cmd.SpuCreateCmd;
import com.lemnt.mall.application.catalog.cmd.SpuShelfCmd;
import com.lemnt.mall.application.catalog.cmd.SpuUpdateCmd;
import com.lemnt.mall.application.catalog.cmd.StockAdjustCmd;
import com.lemnt.mall.application.catalog.qry.SpuPageQry;

/**
 * 目录请求转换器。
 */
public final class CatalogReqConverter {
    private CatalogReqConverter() {}

    /**
     * 分类。
     */
    public static CategorySaveCmd category(Long id, CategorySaveReq req) {
        return new CategorySaveCmd(
                id, req.parentId(), req.name(), req.picUrl(), req.sort(), req.status());
    }

    /**
     * 品牌。
     */
    public static BrandSaveCmd brand(Long id, BrandSaveReq req) {
        return new BrandSaveCmd(
                id, req.name(), req.picUrl(), req.description(), req.sort(), req.status());
    }

    /**
     * 创建。
     */
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

    /**
     * 更新。
     */
    public static SpuUpdateCmd update(Long id, SpuUpdateReq req) {
        return new SpuUpdateCmd(
                id, req.name(), req.categoryId(), req.brandId(), req.description(), req.image());
    }

    /**
     * 转换分页查询或分页响应。
     */
    public static SpuPageQry page(SpuPageReq req) {
        return new SpuPageQry(req.name(), req.status(), req.pageNum(), req.pageSize());
    }

    /**
     * 上架状态。
     */
    public static SpuShelfCmd shelf(Long id, SpuShelfReq req) {
        return new SpuShelfCmd(id, req.onShelf());
    }

    /**
     * 商品SKU。
     */
    public static SkuUpdateCmd sku(Long id, SkuUpdateReq req) {
        return new SkuUpdateCmd(id, req.name(), req.price(), req.image());
    }

    /**
     * 库存。
     */
    public static StockAdjustCmd stock(Long id, StockAdjustReq req) {
        return new StockAdjustCmd(id, req.requestNo(), req.delta(), req.reason());
    }

    /**
     * 分类。
     */
    public static CategorySaveCmd category(Long id, MallCategoryCreateReq req) {
        return new CategorySaveCmd(
                id, req.parentId(), req.name(), req.picUrl(), req.sort(), req.status());
    }

    /**
     * 分类。
     */
    public static CategorySaveCmd category(Long id, MallCategoryUpdateReq req) {
        return new CategorySaveCmd(
                id, req.parentId(), req.name(), req.picUrl(), req.sort(), req.status());
    }

    /**
     * 品牌。
     */
    public static BrandSaveCmd brand(Long id, MallBrandCreateReq req) {
        return new BrandSaveCmd(
                id, req.name(), req.picUrl(), req.description(), req.sort(), req.status());
    }

    /**
     * 品牌。
     */
    public static BrandSaveCmd brand(Long id, MallBrandUpdateReq req) {
        return new BrandSaveCmd(
                id, req.name(), req.picUrl(), req.description(), req.sort(), req.status());
    }
}
