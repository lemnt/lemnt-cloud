package com.lemnt.mall.adapter.app.promotion.converter;

import com.lemnt.mall.adapter.app.promotion.req.AppSeckillPageReq;
import com.lemnt.mall.adapter.app.promotion.res.AppSeckillActivityRes;
import com.lemnt.mall.adapter.app.promotion.res.AppSeckillConfigRes;
import com.lemnt.mall.adapter.app.promotion.res.AppSeckillDetailRes;
import com.lemnt.mall.adapter.app.promotion.res.AppSeckillNowRes;
import com.lemnt.mall.adapter.app.promotion.res.AppSeckillProductRes;
import com.lemnt.mall.application.promotion.dto.SeckillAppActivityDTO;
import com.lemnt.mall.application.promotion.dto.SeckillAppDetailDTO;
import com.lemnt.mall.application.promotion.dto.SeckillConfigDTO;
import com.lemnt.mall.application.promotion.dto.SeckillNowDTO;
import com.lemnt.mall.application.promotion.qry.SeckillAppActivityPageQry;

/** App 秒杀响应转换。 */
public final class AppSeckillConverter {
    private AppSeckillConverter() {}

    public static SeckillAppActivityPageQry page(AppSeckillPageReq req) {
        return new SeckillAppActivityPageQry(req.configId(), req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }

    public static AppSeckillConfigRes config(SeckillConfigDTO dto) {
        return new AppSeckillConfigRes(dto.id(), dto.name(), dto.startTime(), dto.endTime(),
                dto.sliderPicUrls());
    }

    public static AppSeckillActivityRes activity(SeckillAppActivityDTO dto) {
        return new AppSeckillActivityRes(dto.id(), dto.name(), dto.spuId(), dto.spuName(), dto.picUrl(),
                dto.marketPrice(), dto.status(), dto.stock(), dto.totalStock(), dto.seckillPrice());
    }

    public static AppSeckillNowRes now(SeckillNowDTO dto) {
        return new AppSeckillNowRes(dto.config() == null ? null : config(dto.config()),
                dto.activities().stream().map(AppSeckillConverter::activity).toList());
    }

    public static AppSeckillDetailRes detail(SeckillAppDetailDTO dto) {
        return new AppSeckillDetailRes(dto.id(), dto.name(), dto.status(), dto.startTime(), dto.endTime(),
                dto.spuId(), dto.totalLimitCount(), dto.singleLimitCount(), dto.stock(), dto.totalStock(),
                dto.products().stream().map(item -> new AppSeckillProductRes(item.skuId(),
                        item.seckillPrice(), item.stock())).toList());
    }
}
