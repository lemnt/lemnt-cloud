package com.lemnt.member.adapter.web.growth.converter;

import com.lemnt.member.adapter.web.growth.req.ConfigSaveReq;
import com.lemnt.member.adapter.web.growth.req.LevelAssignReq;
import com.lemnt.member.adapter.web.growth.req.MemberLevelCreateReq;
import com.lemnt.member.adapter.web.growth.req.MemberLevelUpdateReq;
import com.lemnt.member.adapter.web.growth.req.MemberUserExperienceReq;
import com.lemnt.member.adapter.web.growth.req.MemberUserPointsReq;
import com.lemnt.member.adapter.web.growth.req.SignConfigSaveReq;
import com.lemnt.member.adapter.web.user.req.MemberUserExperienceRecordsReq;
import com.lemnt.member.adapter.web.user.req.MemberUserLevelRecordsReq;
import com.lemnt.member.adapter.web.user.req.MemberUserPointsRecordsReq;
import com.lemnt.member.adapter.web.user.req.MemberUserSignsReq;
import com.lemnt.member.application.growth.cmd.AssetAdjustCmd;
import com.lemnt.member.application.growth.cmd.ConfigSaveCmd;
import com.lemnt.member.application.growth.cmd.LevelAssignCmd;
import com.lemnt.member.application.growth.cmd.LevelSaveCmd;
import com.lemnt.member.application.growth.cmd.SignConfigSaveCmd;
import com.lemnt.member.application.growth.qry.AssetRecordPageQry;
import com.lemnt.member.application.growth.qry.LevelRecordsPageQry;
import com.lemnt.member.application.growth.qry.SignRecordPageQry;

/** 会员成长权益转换器。 */
public final class MemberGrowthReqConverter {

    private MemberGrowthReqConverter() {}

    /**
     * 等级。
     */
    public static LevelAssignCmd level(Long id, LevelAssignReq r) {
        return new LevelAssignCmd(id, r.levelId(), r.requestNo(), r.reason());
    }

    /**
     * 签到配置。
     */
    public static SignConfigSaveCmd signConfig(SignConfigSaveReq r) {
        return new SignConfigSaveCmd(r.day(), r.point(), r.experience(), r.status());
    }

    /**
     * 配置。
     */
    public static ConfigSaveCmd config(ConfigSaveReq r) {
        return new ConfigSaveCmd(
                r.pointTradeDeductEnable(),
                r.pointTradeDeductUnitPrice(),
                r.pointTradeDeductMaxPrice(),
                r.pointTradeGivePoint());
    }

    /**
     * 等级记录。
     */
    public static LevelRecordsPageQry levelRecords(Long id, MemberUserLevelRecordsReq r) {
        return new LevelRecordsPageQry(id, (r.pageNum() == null ? 1 : r.pageNum()), (r.pageSize() == null ? 20 : r.pageSize()));
    }

    /**
     * 查询签到记录。
     */
    public static SignRecordPageQry signs(Long id, MemberUserSignsReq r) {
        return new SignRecordPageQry(id, (r.pageNum() == null ? 1 : r.pageNum()), (r.pageSize() == null ? 20 : r.pageSize()));
    }

    /**
     * 查询会员资产记录。
     */
    public static AssetRecordPageQry assets(Long id, String type, MemberUserPointsRecordsReq r) {
        return new AssetRecordPageQry(id, type, (r.pageNum() == null ? 1 : r.pageNum()), (r.pageSize() == null ? 20 : r.pageSize()));
    }

    /**
     * 查询会员资产记录。
     */
    public static AssetRecordPageQry assets(Long id, String type, MemberUserExperienceRecordsReq r) {
        return new AssetRecordPageQry(id, type, (r.pageNum() == null ? 1 : r.pageNum()), (r.pageSize() == null ? 20 : r.pageSize()));
    }

    /**
     * 资产。
     */
    public static AssetAdjustCmd asset(Long id, String type, MemberUserPointsReq r) {
        return new AssetAdjustCmd(id, type, r.delta(), r.requestNo(), r.reason());
    }

    /**
     * 资产。
     */
    public static AssetAdjustCmd asset(Long id, String type, MemberUserExperienceReq r) {
        return new AssetAdjustCmd(id, type, r.delta(), r.requestNo(), r.reason());
    }

    /**
     * 等级。
     */
    public static LevelSaveCmd level(Long id, MemberLevelCreateReq r) {
        return new LevelSaveCmd(
                id,
                r.name(),
                r.level(),
                r.experience(),
                r.discountPercent(),
                r.icon(),
                r.backgroundUrl(),
                r.status());
    }

    /**
     * 等级。
     */
    public static LevelSaveCmd level(Long id, MemberLevelUpdateReq r) {
        return new LevelSaveCmd(
                id,
                r.name(),
                r.level(),
                r.experience(),
                r.discountPercent(),
                r.icon(),
                r.backgroundUrl(),
                r.status());
    }
}
