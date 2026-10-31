package com.lemnt.mall.adapter.web.promotion.converter;

import com.lemnt.mall.adapter.web.promotion.req.PointActivityCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.PointActivityPageReq;
import com.lemnt.mall.adapter.web.promotion.req.PointActivityUpdateReq;
import com.lemnt.mall.adapter.web.promotion.req.PointProductReq;
import com.lemnt.mall.adapter.web.promotion.res.PointActivityRes;
import com.lemnt.mall.adapter.web.promotion.res.PointProductRes;
import com.lemnt.mall.application.promotion.cmd.PointActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.PointActivityUpdateCmd;
import com.lemnt.mall.application.promotion.cmd.PointProductCmd;
import com.lemnt.mall.application.promotion.dto.PointActivityDTO;
import com.lemnt.mall.application.promotion.qry.PointActivityPageQry;

import java.util.List;

/** 积分商城请求及响应转换。 */
public final class PointConverter {
    private PointConverter() {}

    private static List<PointProductCmd> products(List<PointProductReq> values) {
        return values.stream().map(value -> new PointProductCmd(value.skuId(), value.exchangeCount(),
                value.point(), value.price(), value.stock())).toList();
    }

    public static PointActivityCreateCmd create(PointActivityCreateReq req) {
        return new PointActivityCreateCmd(req.spuId(), req.remark(), req.sort(), products(req.products()));
    }

    public static PointActivityUpdateCmd update(Long id, PointActivityUpdateReq req) {
        return new PointActivityUpdateCmd(id, req.spuId(), req.remark(), req.sort(),
                products(req.products()));
    }

    public static PointActivityPageQry page(PointActivityPageReq req) {
        return new PointActivityPageQry(req.status(), req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }

    public static PointActivityRes activity(PointActivityDTO dto) {
        return new PointActivityRes(dto.id(), dto.spuId(), dto.spuName(), dto.picUrl(), dto.status(),
                dto.remark(), dto.sort(), dto.stock(), dto.totalStock(), dto.point(), dto.price(),
                dto.marketPrice(), dto.products().stream().map(item -> new PointProductRes(item.id(),
                        item.skuId(), item.spuId(), item.exchangeCount(), item.point(), item.price(),
                        item.stock())).toList());
    }
}
