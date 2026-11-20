package com.lemnt.promotion.adapter.web.promotion.converter;

import com.lemnt.promotion.adapter.web.promotion.req.DiyPagePageReq;
import com.lemnt.promotion.adapter.web.promotion.req.DiyPageSaveReq;
import com.lemnt.promotion.adapter.web.promotion.req.DiyTemplatePageReq;
import com.lemnt.promotion.adapter.web.promotion.req.DiyTemplateSaveReq;
import com.lemnt.promotion.adapter.web.promotion.res.DiyPageRes;
import com.lemnt.promotion.adapter.web.promotion.res.DiyTemplateRes;
import com.lemnt.promotion.application.promotion.cmd.DiyPageSaveCmd;
import com.lemnt.promotion.application.promotion.cmd.DiyTemplateSaveCmd;
import com.lemnt.promotion.application.promotion.dto.DiyPageDTO;
import com.lemnt.promotion.application.promotion.dto.DiyTemplateDTO;
import com.lemnt.promotion.application.promotion.qry.DiyPagePageQry;
import com.lemnt.promotion.application.promotion.qry.DiyTemplatePageQry;

/**
 * 装修请求及响应转换。
 */
public final class DiyConverter {
    private DiyConverter() {
    }

    public static DiyTemplatePageQry page(DiyTemplatePageReq req) {
        return new DiyTemplatePageQry(
            req.name(),
            req.used(),
            req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }

    public static DiyPagePageQry page(DiyPagePageReq req) {
        return new DiyPagePageQry(
            req.templateId(),
            req.name(),
            req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }

    public static DiyTemplateSaveCmd template(DiyTemplateSaveReq req) {
        return new DiyTemplateSaveCmd(null, req.name(), req.remark(), req.previewPicUrls());
    }

    public static DiyTemplateSaveCmd template(Long id, DiyTemplateSaveReq req) {
        return new DiyTemplateSaveCmd(id, req.name(), req.remark(), req.previewPicUrls());
    }

    public static DiyPageSaveCmd page(DiyPageSaveReq req) {
        return new DiyPageSaveCmd(
            null, req.templateId(), req.name(), req.remark(), req.previewPicUrls());
    }

    public static DiyPageSaveCmd page(Long id, DiyPageSaveReq req) {
        return new DiyPageSaveCmd(
            id, req.templateId(), req.name(), req.remark(), req.previewPicUrls());
    }

    public static DiyTemplateRes template(DiyTemplateDTO value) {
        return new DiyTemplateRes(
            value.id(),
            value.name(),
            value.used(),
            value.usedTime(),
            value.remark(),
            value.previewPicUrls(),
            value.property());
    }

    public static DiyPageRes page(DiyPageDTO value) {
        return new DiyPageRes(
            value.id(),
            value.templateId(),
            value.name(),
            value.remark(),
            value.previewPicUrls(),
            value.property());
    }
}
