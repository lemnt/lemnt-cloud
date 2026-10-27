package com.lemnt.member.adapter.web.growth.converter;

import com.lemnt.member.adapter.web.growth.req.MemberRecordPointRecordsReq;
import com.lemnt.member.application.growth.qry.AssetRecordAllPageQry;
import com.lemnt.member.adapter.web.growth.req.MemberRecordExperienceRecordsReq;
import com.lemnt.member.adapter.web.growth.req.MemberRecordLevelRecordsReq;
import com.lemnt.member.application.growth.qry.LevelRecordAllPageQry;
import com.lemnt.member.adapter.web.growth.req.MemberRecordSigninRecordsReq;
import com.lemnt.member.application.growth.qry.SignRecordAllPageQry;

/** 会员成长记录分页请求转换器。 */
public final class MemberReqConverter {

    private MemberReqConverter() {}

    /** 转换资产流水分页请求。 */
    public static AssetRecordAllPageQry toPageQry(String assetType, MemberRecordPointRecordsReq req) {
        return new AssetRecordAllPageQry(assetType, req.memberId(),
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }

    /** 转换资产流水分页请求。 */
    public static AssetRecordAllPageQry toPageQry(String assetType, MemberRecordExperienceRecordsReq req) {
        return new AssetRecordAllPageQry(assetType, req.memberId(),
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }

    /** 转换等级变更流水分页请求。 */
    public static LevelRecordAllPageQry toLevelRecordPageQry(MemberRecordLevelRecordsReq req) {
        return new LevelRecordAllPageQry(req.memberId(),
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }

    /** 转换签到记录分页请求。 */
    public static SignRecordAllPageQry toSignRecordPageQry(MemberRecordSigninRecordsReq req) {
        return new SignRecordAllPageQry(req.memberId(),
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }
}
