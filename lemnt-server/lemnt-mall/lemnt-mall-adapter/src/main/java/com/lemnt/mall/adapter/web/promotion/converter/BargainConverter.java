package com.lemnt.mall.adapter.web.promotion.converter;

import com.lemnt.mall.adapter.web.promotion.req.BargainActivityCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.BargainActivityPageReq;
import com.lemnt.mall.adapter.web.promotion.req.BargainActivityUpdateReq;
import com.lemnt.mall.adapter.web.promotion.req.BargainHelpPageReq;
import com.lemnt.mall.adapter.web.promotion.req.BargainRecordPageReq;
import com.lemnt.mall.adapter.web.promotion.res.BargainActivityRes;
import com.lemnt.mall.adapter.web.promotion.res.BargainHelpRes;
import com.lemnt.mall.adapter.web.promotion.res.BargainRecordRes;
import com.lemnt.mall.application.promotion.cmd.BargainActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.BargainActivityUpdateCmd;
import com.lemnt.mall.application.promotion.dto.BargainActivityDTO;
import com.lemnt.mall.application.promotion.dto.BargainHelpDTO;
import com.lemnt.mall.application.promotion.dto.BargainRecordDTO;
import com.lemnt.mall.application.promotion.qry.BargainActivityPageQry;
import com.lemnt.mall.application.promotion.qry.BargainHelpPageQry;
import com.lemnt.mall.application.promotion.qry.BargainRecordPageQry;

/** 砍价请求及响应转换。 */
public final class BargainConverter {
    private BargainConverter() {}

    public static BargainActivityCreateCmd create(BargainActivityCreateReq req) {
        return new BargainActivityCreateCmd(req.name(), req.startTime(), req.endTime(), req.spuId(),
                req.skuId(), req.bargainFirstPrice(), req.bargainMinPrice(), req.stock(),
                req.helpMaxCount(), req.bargainCount(), req.totalLimitCount(), req.randomMinPrice(),
                req.randomMaxPrice());
    }

    public static BargainActivityUpdateCmd update(Long id, BargainActivityUpdateReq req) {
        return new BargainActivityUpdateCmd(id, req.name(), req.startTime(), req.endTime(), req.spuId(),
                req.skuId(), req.bargainFirstPrice(), req.bargainMinPrice(), req.stock(),
                req.helpMaxCount(), req.bargainCount(), req.totalLimitCount(), req.randomMinPrice(),
                req.randomMaxPrice());
    }

    public static BargainActivityPageQry page(BargainActivityPageReq req) {
        return new BargainActivityPageQry(req.name(), req.status(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }

    public static BargainRecordPageQry recordPage(BargainRecordPageReq req) {
        return new BargainRecordPageQry(req.userId(), req.activityId(), req.status(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }

    public static BargainHelpPageQry helpPage(BargainHelpPageReq req) {
        return new BargainHelpPageQry(req.recordId(), req.userId(), req.activityId(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }

    public static BargainActivityRes activity(BargainActivityDTO dto) {
        return new BargainActivityRes(dto.id(), dto.name(), dto.startTime(), dto.endTime(), dto.status(),
                dto.spuId(), dto.spuName(), dto.picUrl(), dto.skuPrice(), dto.skuId(),
                dto.bargainFirstPrice(), dto.bargainMinPrice(), dto.stock(), dto.totalStock(),
                dto.helpMaxCount(), dto.bargainCount(), dto.totalLimitCount(), dto.randomMinPrice(),
                dto.randomMaxPrice(), dto.successCount());
    }

    public static BargainRecordRes record(BargainRecordDTO dto) {
        return new BargainRecordRes(dto.id(), dto.userId(), dto.activityId(), dto.spuId(), dto.spuName(),
                dto.picUrl(), dto.skuId(), dto.bargainFirstPrice(), dto.bargainPrice(), dto.status(),
                dto.endTime(), dto.orderId());
    }

    public static BargainHelpRes help(BargainHelpDTO dto) {
        return new BargainHelpRes(dto.id(), dto.activityId(), dto.recordId(), dto.userId(),
                dto.reducePrice());
    }
}
