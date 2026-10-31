package com.lemnt.mall.adapter.web.promotion.converter;

import com.lemnt.mall.adapter.web.promotion.req.CombinationActivityCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.CombinationActivityPageReq;
import com.lemnt.mall.adapter.web.promotion.req.CombinationActivityUpdateReq;
import com.lemnt.mall.adapter.web.promotion.req.CombinationProductReq;
import com.lemnt.mall.adapter.web.promotion.req.CombinationRecordPageReq;
import com.lemnt.mall.adapter.web.promotion.res.CombinationActivityRes;
import com.lemnt.mall.adapter.web.promotion.res.CombinationProductRes;
import com.lemnt.mall.adapter.web.promotion.res.CombinationRecordRes;
import com.lemnt.mall.application.promotion.cmd.CombinationActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.CombinationActivityUpdateCmd;
import com.lemnt.mall.application.promotion.cmd.CombinationProductCmd;
import com.lemnt.mall.application.promotion.dto.CombinationActivityDTO;
import com.lemnt.mall.application.promotion.dto.CombinationRecordDTO;
import com.lemnt.mall.application.promotion.qry.CombinationActivityPageQry;
import com.lemnt.mall.application.promotion.qry.CombinationRecordPageQry;

import java.util.List;

/** 拼团请求及响应转换。 */
public final class CombinationConverter {
    private CombinationConverter() {}

    private static List<CombinationProductCmd> products(List<CombinationProductReq> values) {
        return values.stream().map(value -> new CombinationProductCmd(value.skuId(), value.combinationPrice()))
                .toList();
    }

    public static CombinationActivityCreateCmd create(CombinationActivityCreateReq req) {
        return new CombinationActivityCreateCmd(req.name(), req.spuId(), req.totalLimitCount(),
                req.singleLimitCount(), req.startTime(), req.endTime(), req.userSize(), req.virtualGroup(),
                req.limitDuration(), products(req.products()));
    }

    public static CombinationActivityUpdateCmd update(Long id, CombinationActivityUpdateReq req) {
        return new CombinationActivityUpdateCmd(id, req.name(), req.spuId(), req.totalLimitCount(),
                req.singleLimitCount(), req.startTime(), req.endTime(), req.userSize(), req.virtualGroup(),
                req.limitDuration(), products(req.products()));
    }

    public static CombinationActivityPageQry page(CombinationActivityPageReq req) {
        return new CombinationActivityPageQry(req.name(), req.status(), req.spuId(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }

    public static CombinationRecordPageQry recordPage(CombinationRecordPageReq req) {
        return new CombinationRecordPageQry(req.userId(), req.activityId(), req.status(), req.headId(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }

    public static CombinationActivityRes activity(CombinationActivityDTO dto) {
        return new CombinationActivityRes(dto.id(), dto.name(), dto.spuId(), dto.spuName(), dto.picUrl(),
                dto.combinationPrice(), dto.totalLimitCount(), dto.singleLimitCount(), dto.startTime(),
                dto.endTime(), dto.userSize(), dto.virtualGroup(), dto.status(), dto.limitDuration(),
                dto.products().stream().map(item -> new CombinationProductRes(item.skuId(),
                        item.combinationPrice())).toList());
    }

    public static CombinationRecordRes record(CombinationRecordDTO dto) {
        return new CombinationRecordRes(dto.id(), dto.activityId(), dto.combinationPrice(), dto.spuId(),
                dto.spuName(), dto.picUrl(), dto.skuId(), dto.count(), dto.userId(), dto.nickname(),
                dto.avatar(), dto.headId(), dto.status(), dto.orderId(), dto.userSize(), dto.userCount(),
                dto.virtualGroup(), dto.expireTime(), dto.startTime(), dto.endTime());
    }
}
