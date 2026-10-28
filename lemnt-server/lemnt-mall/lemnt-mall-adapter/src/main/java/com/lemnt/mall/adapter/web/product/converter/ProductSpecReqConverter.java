package com.lemnt.mall.adapter.web.product.converter;

import com.lemnt.mall.adapter.web.product.req.ProductSpecCreateReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpecPageReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpecSimpleReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpecUpdateReq;
import com.lemnt.mall.application.product.cmd.ProductSpecCreateCmd;
import com.lemnt.mall.application.product.cmd.ProductSpecUpdateCmd;
import com.lemnt.mall.application.product.qry.ProductSpecPageQry;
import com.lemnt.mall.application.product.qry.ProductSpecSimpleListQry;

/** 规格字典请求转换。 */
public final class ProductSpecReqConverter {
    private ProductSpecReqConverter() {
    }

    /** 转换新增请求。 */
    public static ProductSpecCreateCmd create(ProductSpecCreateReq req) {
        return new ProductSpecCreateCmd(req.name(), req.remark());
    }

    /** 转换修改请求。 */
    public static ProductSpecUpdateCmd update(Long id, ProductSpecUpdateReq req) {
        return new ProductSpecUpdateCmd(id, req.name(), req.remark());
    }

    /** 转换分页请求。 */
    public static ProductSpecPageQry page(ProductSpecPageReq req) {
        return new ProductSpecPageQry(req.name(), req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }

    /** 转换选择查询。 */
    public static ProductSpecSimpleListQry simple(ProductSpecSimpleReq req) {
        return new ProductSpecSimpleListQry(req.name(), req.limit() == null ? 20 : req.limit());
    }
}
