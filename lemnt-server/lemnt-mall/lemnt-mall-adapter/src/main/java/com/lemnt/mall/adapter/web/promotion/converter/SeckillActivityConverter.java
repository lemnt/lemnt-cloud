package com.lemnt.mall.adapter.web.promotion.converter;

import com.lemnt.mall.adapter.web.promotion.req.SeckillActivityCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.SeckillActivityPageReq;
import com.lemnt.mall.adapter.web.promotion.req.SeckillActivityUpdateReq;
import com.lemnt.mall.adapter.web.promotion.res.SeckillActivityRes;
import com.lemnt.mall.adapter.web.promotion.res.SeckillProductRes;
import com.lemnt.mall.application.promotion.cmd.SeckillActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.SeckillActivityUpdateCmd;
import com.lemnt.mall.application.promotion.cmd.SeckillProductCmd;
import com.lemnt.mall.application.promotion.dto.SeckillActivityDTO;
import com.lemnt.mall.application.promotion.dto.SeckillProductDTO;
import com.lemnt.mall.application.promotion.qry.SeckillActivityPageQry;

/** 秒杀活动请求及响应转换。 */
public final class SeckillActivityConverter {
    private SeckillActivityConverter() {}

    private static java.util.List<SeckillProductCmd> products(
            java.util.List<com.lemnt.mall.adapter.web.promotion.req.SeckillProductReq> values) {
        return values.stream().map(value -> new SeckillProductCmd(value.skuId(), value.seckillPrice(),
                value.stock())).toList();
    }

    public static SeckillActivityCreateCmd create(SeckillActivityCreateReq req) {
        return new SeckillActivityCreateCmd(req.spuId(), req.name(), req.remark(), req.startTime(),
                req.endTime(), req.sort(), req.configIds(), req.totalLimitCount(), req.singleLimitCount(),
                products(req.products()));
    }

    public static SeckillActivityUpdateCmd update(Long id, SeckillActivityUpdateReq req) {
        return new SeckillActivityUpdateCmd(id, req.spuId(), req.name(), req.remark(), req.startTime(),
                req.endTime(), req.sort(), req.configIds(), req.totalLimitCount(), req.singleLimitCount(),
                products(req.products()));
    }

    public static SeckillActivityPageQry page(SeckillActivityPageReq req) {
        return new SeckillActivityPageQry(req.name(), req.status(), req.configId(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }

    private static SeckillProductRes product(SeckillProductDTO dto) {
        return new SeckillProductRes(dto.id(), dto.skuId(), dto.spuId(), dto.seckillPrice(), dto.stock(),
                dto.activityStartTime(), dto.activityEndTime());
    }

    public static SeckillActivityRes res(SeckillActivityDTO dto) {
        return new SeckillActivityRes(dto.id(), dto.spuId(), dto.spuName(), dto.picUrl(), dto.seckillPrice(),
                dto.name(), dto.status(), dto.remark(), dto.startTime(), dto.endTime(), dto.sort(),
                dto.configIds(), dto.totalLimitCount(), dto.singleLimitCount(), dto.stock(), dto.totalStock(),
                dto.products().stream().map(SeckillActivityConverter::product).toList());
    }
}
