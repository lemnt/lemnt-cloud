package com.lemnt.member.adapter.app.growth.converter;

import com.lemnt.member.adapter.app.growth.res.AppMemberGrowthExperienceRes;
import com.lemnt.member.adapter.app.growth.res.AppMemberGrowthPointRes;
import com.lemnt.member.adapter.app.growth.res.AppMemberGrowthSignRes;
import com.lemnt.member.adapter.app.growth.res.AppMemberGrowthSignsRes;
import com.lemnt.member.adapter.app.growth.res.LevelRes;
import com.lemnt.member.adapter.app.growth.res.SignConfigRes;
import com.lemnt.member.adapter.app.growth.res.SignSummaryRes;
import com.lemnt.member.application.growth.dto.AssetRecordDTO;
import com.lemnt.member.application.growth.dto.LevelDTO;
import com.lemnt.member.application.growth.dto.SignConfigDTO;
import com.lemnt.member.application.growth.dto.SignRecordDTO;
import com.lemnt.member.application.growth.dto.SignSummaryDTO;

/** 会员成长权益转换器。 */
public final class MemberGrowthResConverter {

    private MemberGrowthResConverter() {}

    /**
     * 等级。
     */
    public static LevelRes level(LevelDTO v) {
        return v == null
                ? null
                : new LevelRes(
                        v.id(),
                        v.name(),
                        v.level(),
                        v.experience(),
                        v.discountPercent(),
                        v.icon(),
                        v.backgroundUrl(),
                        v.status());
    }

    /**
     * 签到配置。
     */
    public static SignConfigRes signConfig(SignConfigDTO v) {
        return v == null ? null : new SignConfigRes(v.day(), v.point(), v.experience(), v.status());
    }

    /**
     * 汇总。
     */
    public static SignSummaryRes summary(
            SignSummaryDTO v) {
        return new SignSummaryRes(v.totalDay(), v.continuousDay(), v.todaySignIn());
    }

    /**
     * 签到记录。
     */
    public static AppMemberGrowthSignRes toAppMemberGrowthSignRes(SignRecordDTO v) {
        return v == null
                ? null
                : new AppMemberGrowthSignRes(
                        v.id(), v.memberId(), v.signDate(), v.day(), v.point(), v.experience());
    }

    /**
     * 签到记录。
     */
    public static AppMemberGrowthSignsRes toAppMemberGrowthSignsRes(SignRecordDTO v) {
        return v == null
                ? null
                : new AppMemberGrowthSignsRes(
                        v.id(), v.memberId(), v.signDate(), v.day(), v.point(), v.experience());
    }

    /**
     * 资产记录。
     */
    public static AppMemberGrowthPointRes toAppMemberGrowthPointRes(AssetRecordDTO v) {
        return v == null
                ? null
                : new AppMemberGrowthPointRes(
                        v.id(),
                        v.memberId(),
                        v.assetType(),
                        v.delta(),
                        v.balance(),
                        v.bizType(),
                        v.bizNo(),
                        v.reason(),
                        v.createTime(),v.pointBizType(),v.title(),v.description());
    }

    /**
     * 资产记录。
     */
    public static AppMemberGrowthExperienceRes toAppMemberGrowthExperienceRes(AssetRecordDTO v) {
        return v == null
                ? null
                : new AppMemberGrowthExperienceRes(
                        v.id(),
                        v.memberId(),
                        v.assetType(),
                        v.delta(),
                        v.balance(),
                        v.bizType(),
                        v.bizNo(),
                        v.reason(),
                        v.createTime());
    }
}
