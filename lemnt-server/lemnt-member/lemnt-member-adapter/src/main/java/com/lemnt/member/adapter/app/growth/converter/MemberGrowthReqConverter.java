package com.lemnt.member.adapter.app.growth.converter;

import com.lemnt.member.adapter.app.growth.req.ExperienceRecordPageReq;
import com.lemnt.member.adapter.app.growth.req.PointRecordPageReq;
import com.lemnt.member.adapter.app.growth.req.SignRecordPageReq;
import com.lemnt.member.application.growth.qry.AssetRecordPageQry;
import com.lemnt.member.application.growth.qry.SignRecordPageQry;

/** 会员成长权益请求转换器。 */
public final class MemberGrowthReqConverter {

    private MemberGrowthReqConverter() {}

    /** 转换本人签到记录分页请求。 */
    public static SignRecordPageQry toSignRecordPageQry(Long memberId, SignRecordPageReq req) {
        return new SignRecordPageQry(memberId,
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }

    /** 转换本人积分记录分页请求，资产类型由接口确定。 */
    public static AssetRecordPageQry toPointRecordPageQry(Long memberId, PointRecordPageReq req) {
        return new AssetRecordPageQry(memberId, "POINT",
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize(), req.addStatus(), req.startTime(), req.endTime());
    }

    /** 转换本人经验记录分页请求，资产类型由接口确定。 */
    public static AssetRecordPageQry toExperienceRecordPageQry(Long memberId, ExperienceRecordPageReq req) {
        return new AssetRecordPageQry(memberId, "EXPERIENCE",
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }
}
