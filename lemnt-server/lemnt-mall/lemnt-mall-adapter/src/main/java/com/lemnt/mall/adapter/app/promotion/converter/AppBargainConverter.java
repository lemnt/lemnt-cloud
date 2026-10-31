package com.lemnt.mall.adapter.app.promotion.converter;

import com.lemnt.mall.adapter.app.promotion.res.AppBargainActivityRes;
import com.lemnt.mall.adapter.app.promotion.res.AppBargainDetailRes;
import com.lemnt.mall.adapter.app.promotion.res.AppBargainHelpRes;
import com.lemnt.mall.adapter.app.promotion.res.AppBargainRecordRes;
import com.lemnt.mall.application.promotion.dto.BargainActivityDTO;
import com.lemnt.mall.application.promotion.dto.BargainDetailDTO;
import com.lemnt.mall.application.promotion.dto.BargainHelpDTO;
import com.lemnt.mall.application.promotion.dto.BargainRecordDTO;

/** App 砍价响应转换。 */
public final class AppBargainConverter {
    private AppBargainConverter() {}

    public static AppBargainActivityRes activity(BargainActivityDTO dto) {
        return new AppBargainActivityRes(dto.id(), dto.name(), dto.spuId(), dto.spuName(), dto.picUrl(),
                dto.skuPrice(), dto.skuId(), dto.bargainFirstPrice(), dto.bargainMinPrice(), dto.stock(),
                dto.totalStock(), dto.helpMaxCount(), dto.bargainCount(), dto.totalLimitCount(),
                dto.startTime(), dto.endTime(), dto.successCount());
    }

    public static AppBargainRecordRes record(BargainRecordDTO dto) {
        return new AppBargainRecordRes(dto.id(), dto.userId(), dto.activityId(), dto.spuId(), dto.spuName(),
                dto.picUrl(), dto.skuId(), dto.bargainFirstPrice(), dto.bargainPrice(), dto.status(),
                dto.endTime(), dto.orderId());
    }

    public static AppBargainHelpRes help(BargainHelpDTO dto) {
        return new AppBargainHelpRes(dto.id(), dto.userId(), dto.reducePrice());
    }

    public static AppBargainDetailRes detail(BargainDetailDTO dto) {
        return new AppBargainDetailRes(activity(dto.activity()), record(dto.record()),
                dto.helps().stream().map(AppBargainConverter::help).toList(), dto.helped());
    }
}
