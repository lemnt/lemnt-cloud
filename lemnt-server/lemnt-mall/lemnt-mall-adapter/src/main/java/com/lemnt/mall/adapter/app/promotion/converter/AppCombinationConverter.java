package com.lemnt.mall.adapter.app.promotion.converter;

import com.lemnt.mall.adapter.app.promotion.res.AppCombinationActivityRes;
import com.lemnt.mall.adapter.app.promotion.res.AppCombinationDetailRes;
import com.lemnt.mall.adapter.app.promotion.res.AppCombinationRecordDetailRes;
import com.lemnt.mall.adapter.app.promotion.res.AppCombinationRecordRes;
import com.lemnt.mall.adapter.app.promotion.res.AppCombinationSummaryRes;
import com.lemnt.mall.application.promotion.dto.CombinationActivityDTO;
import com.lemnt.mall.application.promotion.dto.CombinationAppActivityDTO;
import com.lemnt.mall.application.promotion.dto.CombinationRecordDTO;
import com.lemnt.mall.application.promotion.dto.CombinationRecordDetailDTO;
import com.lemnt.mall.application.promotion.dto.CombinationSummaryDTO;

/** App 拼团响应转换。 */
public final class AppCombinationConverter {
    private AppCombinationConverter() {}

    public static AppCombinationActivityRes activity(CombinationAppActivityDTO dto) {
        return new AppCombinationActivityRes(dto.id(), dto.name(), dto.spuId(), dto.spuName(), dto.picUrl(),
                dto.marketPrice(), dto.combinationPrice(), dto.status(), dto.startTime(), dto.endTime(),
                dto.userSize());
    }

    public static AppCombinationDetailRes detail(CombinationActivityDTO dto, long successCount) {
        return new AppCombinationDetailRes(dto.id(), dto.name(), dto.status(), dto.startTime(), dto.endTime(),
                dto.userSize(), successCount, dto.spuId(), dto.totalLimitCount(), dto.singleLimitCount(),
                dto.products().stream().map(item -> new AppCombinationDetailRes.Product(item.skuId(),
                        item.combinationPrice())).toList());
    }

    public static AppCombinationRecordRes record(CombinationRecordDTO dto) {
        return new AppCombinationRecordRes(dto.id(), dto.activityId(), dto.combinationPrice(), dto.spuId(),
                dto.spuName(), dto.picUrl(), dto.skuId(), dto.count(), dto.userId(), dto.nickname(),
                dto.avatar(), dto.headId(), dto.status(), dto.orderId(), dto.userSize(), dto.userCount(),
                dto.virtualGroup(), dto.expireTime());
    }

    public static AppCombinationRecordDetailRes recordDetail(CombinationRecordDetailDTO dto) {
        return new AppCombinationRecordDetailRes(record(dto.headRecord()),
                dto.memberRecords().stream().map(AppCombinationConverter::record).toList(), dto.orderId());
    }

    public static AppCombinationSummaryRes summary(CombinationSummaryDTO dto) {
        return new AppCombinationSummaryRes(dto.userCount(), dto.avatars());
    }
}
