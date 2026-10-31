package com.lemnt.mall.adapter.web.product.converter;

import com.lemnt.mall.adapter.web.product.req.ProductSpecValueCreateReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpecValuePageReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpecValueSimpleReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpecValueUpdateReq;
import com.lemnt.mall.application.product.cmd.ProductSpecValueCreateCmd;
import com.lemnt.mall.application.product.cmd.ProductSpecValueUpdateCmd;
import com.lemnt.mall.application.product.qry.ProductSpecValuePageQry;
import com.lemnt.mall.application.product.qry.ProductSpecValueSimpleListQry;

/** 规格字典请求转换。 */
public final class ProductSpecValueReqConverter {
    private ProductSpecValueReqConverter() {
    }

    /** 转换新增请求。 */
    public static ProductSpecValueCreateCmd create(ProductSpecValueCreateReq req) {
        return new ProductSpecValueCreateCmd(req.specId(), req.name(), req.remark());
    }

    /** 转换修改请求。 */
    public static ProductSpecValueUpdateCmd update(Long id, ProductSpecValueUpdateReq req) {
        return new ProductSpecValueUpdateCmd(id, req.name(), req.remark());
    }

    /** 转换分页请求。 */
    public static ProductSpecValuePageQry page(ProductSpecValuePageReq req) {
        return new ProductSpecValuePageQry(req.specId(), req.name(), req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }

    /** 转换选择查询。 */
    public static ProductSpecValueSimpleListQry simple(ProductSpecValueSimpleReq req) {
        return new ProductSpecValueSimpleListQry(req.specId(), req.name(), req.limit() == null ? 20 : req.limit());
    }
}
