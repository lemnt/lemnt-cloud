package com.lemnt.product.adapter.web.product.converter;

import com.lemnt.product.adapter.web.product.req.ProductBrandPageReq;
import com.lemnt.product.adapter.web.product.req.ProductBrandSimpleReq;
import com.lemnt.product.adapter.web.product.req.ProductSpuCountReq;
import com.lemnt.product.adapter.web.product.req.ProductSpuExportReq;
import com.lemnt.product.adapter.web.product.req.ProductSpuSimpleReq;
import com.lemnt.product.application.product.qry.ProductBrandPageQry;
import com.lemnt.product.application.product.qry.ProductBrandSimpleListQry;
import com.lemnt.product.application.product.qry.ProductSpuCountQry;
import com.lemnt.product.application.product.qry.ProductSpuExportQry;
import com.lemnt.product.application.product.qry.ProductSpuSimpleListQry;

/**
 * 商品管理新增接口请求转换。
 */
public final class ProductManagementReqConverter {
    private ProductManagementReqConverter() {
    }

    /**
     * 转换品牌分页。
     */
    public static ProductBrandPageQry brands(ProductBrandPageReq req) {
        return new ProductBrandPageQry(
            req.name(),
            req.status(),
            req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }

    /**
     * 转换品牌选择。
     */
    public static ProductBrandSimpleListQry brandSimple(ProductBrandSimpleReq req) {
        return new ProductBrandSimpleListQry(req.name(), req.limit() == null ? 20 : req.limit());
    }

    /**
     * 转换商品统计。
     */
    public static ProductSpuCountQry counts(ProductSpuCountReq req) {
        return new ProductSpuCountQry(req.name(), req.categoryId(), req.brandId());
    }

    /**
     * 转换商品选择。
     */
    public static ProductSpuSimpleListQry simple(ProductSpuSimpleReq req) {
        return new ProductSpuSimpleListQry(req.name(), req.limit() == null ? 20 : req.limit());
    }

    /**
     * 导出使用独立请求，不能通过页码截断数据。
     */
    public static ProductSpuExportQry export(ProductSpuExportReq req) {
        return new ProductSpuExportQry(
            req.name(), req.status(), req.categoryId(), req.brandId(), req.stockStatus());
    }
}
