package com.lemnt.mall.adapter.web.coupon.converter;

import com.lemnt.mall.adapter.web.coupon.req.CouponManagementPageReq;
import com.lemnt.mall.adapter.web.coupon.req.CouponPublicClaimReq;
import com.lemnt.mall.adapter.web.coupon.req.CouponTemplateCreateReq;
import com.lemnt.mall.adapter.web.coupon.req.CouponTemplateEnableReq;
import com.lemnt.mall.adapter.web.coupon.req.CouponTemplateUpdateReq;
import com.lemnt.mall.application.coupon.cmd.CouponPublicClaimCmd;
import com.lemnt.mall.application.coupon.cmd.CouponTemplateCreateCmd;
import com.lemnt.mall.application.coupon.cmd.CouponTemplateEnableCmd;
import com.lemnt.mall.application.coupon.cmd.CouponTemplateUpdateCmd;
import com.lemnt.mall.application.coupon.qry.CouponManagementPageQry;

/** 按业务域转换接口与应用数据。 */
public final class CouponReqConverter {
    private CouponReqConverter() {}

    /**
     * 模板。
     */
    public static CouponTemplateCreateCmd template(CouponTemplateCreateReq req) {
        return new CouponTemplateCreateCmd(
                req.code(),
                req.name(),
                req.kind(),
                req.threshold(),
                req.fixedAmount(),
                req.discountPercent(),
                req.discountLimit(),
                req.totalCount(),
                req.validFrom(),
                req.validUntil(),
                req.description(), req.takeType() == null ? "ADMIN" : req.takeType(), req.takeLimitCount() == null ? 1 : req.takeLimitCount(), req.productScope() == null ? "ALL" : req.productScope(), req.productScopeValues(), req.validityType() == null ? "DATE" : req.validityType(), req.fixedStartTerm(), req.fixedEndTerm());
    }

    /** 转换模板修改请求。 */
    public static CouponTemplateUpdateCmd update(Long id, CouponTemplateUpdateReq req) {
        return new CouponTemplateUpdateCmd(
                id,
                req.code(),
                req.name(),
                req.kind(),
                req.threshold(),
                req.fixedAmount(),
                req.discountPercent(),
                req.discountLimit(),
                req.totalCount(),
                req.validFrom(),
                req.validUntil(),
                req.description(), req.takeType() == null ? "ADMIN" : req.takeType(), req.takeLimitCount() == null ? 1 : req.takeLimitCount(), req.productScope() == null ? "ALL" : req.productScope(), req.productScopeValues(), req.validityType() == null ? "DATE" : req.validityType(), req.fixedStartTerm(), req.fixedEndTerm());
    }

    /**
     * 启用。
     */
    public static CouponTemplateEnableCmd enable(Long id, CouponTemplateEnableReq req) {
        return new CouponTemplateEnableCmd(id, req.enabled());
    }

    /**
     * 转换公开领取配置命令。
     */
    public static CouponPublicClaimCmd setting(Long t, CouponPublicClaimReq r) {
        return new CouponPublicClaimCmd(t, r.enabled());
    }
    /** 转换独立的后台分页请求。 */
    public static CouponManagementPageQry page(
            CouponManagementPageReq req) {
        return new CouponManagementPageQry(req.memberId(), req.templateId(), req.status(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }
}
