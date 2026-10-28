package com.lemnt.mall.adapter.web.promotion.converter;

import com.lemnt.mall.adapter.web.promotion.req.DiscountActivityCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.DiscountActivityPageReq;
import com.lemnt.mall.adapter.web.promotion.req.DiscountActivityUpdateReq;
import com.lemnt.mall.application.promotion.cmd.DiscountActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.DiscountActivityUpdateCmd;
import com.lemnt.mall.application.promotion.qry.DiscountActivityPageQry;
import com.lemnt.mall.domain.promotion.model.DiscountProduct;

/** 活动请求转换。 */
public final class DiscountActivityReqConverter {
    private DiscountActivityReqConverter() {}

    public static DiscountActivityCreateCmd create(DiscountActivityCreateReq req) {
        return new DiscountActivityCreateCmd(
                req.name(),
                req.startTime(),
                req.endTime(),
                req.remark(),
                req.products().stream().map(p -> new DiscountProduct(null, null, p.spuId(), p.skuId(),
                    p.discountType(), p.discountPercent(), p.discountPrice())).toList());
    }

    public static DiscountActivityUpdateCmd update(Long id, DiscountActivityUpdateReq req) {
        return new DiscountActivityUpdateCmd(
                id,
                req.name(),
                req.startTime(),
                req.endTime(),
                req.remark(),
                req.products().stream().map(p -> new DiscountProduct(null, null, p.spuId(), p.skuId(),
                    p.discountType(), p.discountPercent(), p.discountPrice())).toList());
    }

    public static DiscountActivityPageQry page(DiscountActivityPageReq req) {
        return new DiscountActivityPageQry(req.name(), req.status(), req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }
}
