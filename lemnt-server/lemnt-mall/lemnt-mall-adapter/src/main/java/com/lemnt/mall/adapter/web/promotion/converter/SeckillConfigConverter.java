package com.lemnt.mall.adapter.web.promotion.converter;

import com.lemnt.mall.adapter.web.promotion.req.SeckillConfigCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.SeckillConfigPageReq;
import com.lemnt.mall.adapter.web.promotion.req.SeckillConfigUpdateReq;
import com.lemnt.mall.adapter.web.promotion.res.SeckillConfigRes;
import com.lemnt.mall.adapter.web.promotion.res.SeckillConfigSimpleRes;
import com.lemnt.mall.application.promotion.cmd.SeckillConfigCreateCmd;
import com.lemnt.mall.application.promotion.cmd.SeckillConfigUpdateCmd;
import com.lemnt.mall.application.promotion.dto.SeckillConfigDTO;
import com.lemnt.mall.application.promotion.qry.SeckillConfigPageQry;

/** 秒杀时段请求及响应转换。 */
public final class SeckillConfigConverter {
    private SeckillConfigConverter() {}

    public static SeckillConfigCreateCmd create(SeckillConfigCreateReq req) {
        return new SeckillConfigCreateCmd(req.name(), req.startTime(), req.endTime(), req.sliderPicUrls(),
                req.status());
    }

    public static SeckillConfigUpdateCmd update(Long id, SeckillConfigUpdateReq req) {
        return new SeckillConfigUpdateCmd(id, req.name(), req.startTime(), req.endTime(), req.sliderPicUrls(),
                req.status());
    }

    public static SeckillConfigPageQry page(SeckillConfigPageReq req) {
        return new SeckillConfigPageQry(req.name(), req.status(), req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }

    public static SeckillConfigRes res(SeckillConfigDTO dto) {
        return new SeckillConfigRes(dto.id(), dto.name(), dto.startTime(), dto.endTime(), dto.sliderPicUrls(),
                dto.status(), dto.activityCount());
    }

    public static SeckillConfigSimpleRes simple(SeckillConfigDTO dto) {
        return new SeckillConfigSimpleRes(dto.id(), dto.name(), dto.startTime(), dto.endTime(),
                dto.sliderPicUrls());
    }
}
