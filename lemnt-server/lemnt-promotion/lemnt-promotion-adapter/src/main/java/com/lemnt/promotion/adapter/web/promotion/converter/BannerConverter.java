package com.lemnt.promotion.adapter.web.promotion.converter;

import com.lemnt.promotion.adapter.web.promotion.req.BannerCreateReq;
import com.lemnt.promotion.adapter.web.promotion.req.BannerPageReq;
import com.lemnt.promotion.adapter.web.promotion.req.BannerUpdateReq;
import com.lemnt.promotion.adapter.web.promotion.res.BannerRes;
import com.lemnt.promotion.application.promotion.cmd.BannerCreateCmd;
import com.lemnt.promotion.application.promotion.cmd.BannerUpdateCmd;
import com.lemnt.promotion.application.promotion.dto.BannerDTO;
import com.lemnt.promotion.application.promotion.qry.BannerPageQry;

/** Banner 请求及响应转换。 */
public final class BannerConverter {
    private BannerConverter() {}

    public static BannerCreateCmd create(BannerCreateReq req) {
        return new BannerCreateCmd(
                req.title(),
                req.url(),
                req.picUrl(),
                req.sort(),
                req.status(),
                req.position(),
                req.memo());
    }

    public static BannerUpdateCmd update(Long id, BannerUpdateReq req) {
        return new BannerUpdateCmd(
                id,
                req.title(),
                req.url(),
                req.picUrl(),
                req.sort(),
                req.status(),
                req.position(),
                req.memo());
    }

    public static BannerPageQry page(BannerPageReq req) {
        return new BannerPageQry(
                req.title(),
                req.status(),
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }

    public static BannerRes res(BannerDTO dto) {
        return new BannerRes(
                dto.id(),
                dto.title(),
                dto.url(),
                dto.picUrl(),
                dto.sort(),
                dto.status(),
                dto.position(),
                dto.memo(),
                dto.browseCount());
    }
}
