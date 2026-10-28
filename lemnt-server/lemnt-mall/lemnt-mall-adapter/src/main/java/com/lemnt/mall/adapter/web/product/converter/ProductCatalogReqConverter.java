package com.lemnt.mall.adapter.web.product.converter;

import com.lemnt.mall.adapter.web.product.req.BrandSaveReq;
import com.lemnt.mall.adapter.web.product.req.CategorySaveReq;
import com.lemnt.mall.adapter.web.product.req.MallBrandCreateReq;
import com.lemnt.mall.adapter.web.product.req.MallBrandUpdateReq;
import com.lemnt.mall.adapter.web.product.req.MallCategoryCreateReq;
import com.lemnt.mall.adapter.web.product.req.MallCategoryUpdateReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpuCreateReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpuPageReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpuShelfReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpuUpdateReq;
import com.lemnt.mall.application.product.cmd.BrandSaveCmd;
import com.lemnt.mall.application.product.cmd.CategorySaveCmd;
import com.lemnt.mall.application.product.cmd.ProductSpuCreateCmd;
import com.lemnt.mall.application.product.cmd.ProductSpuShelfCmd;
import com.lemnt.mall.application.product.cmd.ProductSpuUpdateCmd;
import com.lemnt.mall.application.product.qry.ProductSpuPageQry;
import com.lemnt.mall.domain.product.model.ProductSku;

/**
 * 目录请求转换器。
 */
public final class ProductCatalogReqConverter {
    private ProductCatalogReqConverter() {}

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
    public static ProductSpuCreateCmd create(ProductSpuCreateReq req) {
        return new ProductSpuCreateCmd(
                req.code(),
                req.name(),
                req.categoryId(),
                req.brandId(),
                req.description(),
                req.image(),
                req.skus().stream()
                .map(
                s ->
                new ProductSpuCreateCmd.ProductSkuDraft(
                s.code(),
                s.name(),
                s.price(),
                s.stock(),
                s.image(),
                s.properties(), s.weight(), s.volume(), s.specValueIds()))
                .toList(), req.deliveryTypes(), req.deliveryTemplateId());
    }

    /**
     * 更新。
     */
    public static ProductSpuUpdateCmd update(Long id, ProductSpuUpdateReq req) {
        return new ProductSpuUpdateCmd(
                id, req.name(), req.categoryId(), req.brandId(), req.description(), req.image(),
                req.skus().stream().map(sku -> new ProductSku(
                        sku.id(), id, sku.code(), sku.name(), sku.price(), sku.stock(), sku.image(),
                        sku.properties(), "ON_SHELF", sku.weight(), sku.volume(), sku.specValueIds())).toList(), req.deliveryTypes(), req.deliveryTemplateId());
    }

    /**
     * 转换分页查询或分页响应。
     */
    public static ProductSpuPageQry page(ProductSpuPageReq req) {
        return new ProductSpuPageQry(req.name(), req.status(), req.pageNum(), req.pageSize(), req.categoryId(),
            req.brandId());
    }

    /**
     * 上架状态。
     */
    public static ProductSpuShelfCmd shelf(Long id, ProductSpuShelfReq req) {
        return new ProductSpuShelfCmd(id, req.onShelf());
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
