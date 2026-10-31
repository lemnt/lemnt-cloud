package com.lemnt.mall.adapter.web.product.converter;

import com.lemnt.mall.adapter.web.product.req.BrandPageReq;
import com.lemnt.mall.adapter.web.product.req.BrandSimpleReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpuCountReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpuExportReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpuSimpleReq;
import com.lemnt.mall.application.product.qry.BrandPageQry;
import com.lemnt.mall.application.product.qry.BrandSimpleListQry;
import com.lemnt.mall.application.product.qry.ProductSpuCountQry;
import com.lemnt.mall.application.product.qry.ProductSpuExportQry;
import com.lemnt.mall.application.product.qry.ProductSpuSimpleListQry;

/** 商品管理新增接口请求转换。 */
public final class ProductManagementReqConverter {
    private ProductManagementReqConverter() {
    }

    /** 转换品牌分页。 */
    public static BrandPageQry brands(BrandPageReq req) {
        return new BrandPageQry(req.name(), req.status(), req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }

    /** 转换品牌选择。 */
    public static BrandSimpleListQry brandSimple(BrandSimpleReq req) {
        return new BrandSimpleListQry(req.name(), req.limit() == null ? 20 : req.limit());
    }

    /** 转换商品统计。 */
    public static ProductSpuCountQry counts(ProductSpuCountReq req) {
        return new ProductSpuCountQry(req.name(), req.categoryId(), req.brandId());
    }

    /** 转换商品选择。 */
    public static ProductSpuSimpleListQry simple(ProductSpuSimpleReq req) {
        return new ProductSpuSimpleListQry(req.name(), req.limit() == null ? 20 : req.limit());
    }

    /** 导出使用独立请求，不能通过页码截断数据。 */
    public static ProductSpuExportQry export(ProductSpuExportReq req) {
        return new ProductSpuExportQry(req.name(), req.status(), req.categoryId(), req.brandId(), req.stockStatus());
    }
}
