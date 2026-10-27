package com.lemnt.member.adapter.web.growth.converter;

import com.lemnt.member.adapter.web.growth.res.ConfigRes;
import com.lemnt.member.adapter.web.growth.res.MemberConfigDetailRes;
import com.lemnt.member.adapter.web.growth.res.MemberConfigListRes;
import com.lemnt.member.adapter.web.growth.res.MemberLevelDetailRes;
import com.lemnt.member.adapter.web.growth.res.MemberLevelListRes;
import com.lemnt.member.adapter.web.growth.res.MemberLevelSimpleRes;
import com.lemnt.member.adapter.web.growth.res.MemberRecordExperienceRecordsDetailRes;
import com.lemnt.member.adapter.web.growth.res.MemberRecordExperienceRecordsRes;
import com.lemnt.member.adapter.web.growth.res.MemberRecordLevelRecordsDetailRes;
import com.lemnt.member.adapter.web.growth.res.MemberRecordLevelRecordsRes;
import com.lemnt.member.adapter.web.growth.res.MemberRecordPointRecordsDetailRes;
import com.lemnt.member.adapter.web.growth.res.MemberRecordPointRecordsRes;
import com.lemnt.member.adapter.web.growth.res.MemberRecordSigninRecordsRes;
import com.lemnt.member.adapter.web.growth.res.MemberUserExperienceRecordsRes;
import com.lemnt.member.adapter.web.growth.res.MemberUserExperienceRes;
import com.lemnt.member.adapter.web.growth.res.MemberUserLevelRecordsRes;
import com.lemnt.member.adapter.web.growth.res.MemberUserPointsRecordsRes;
import com.lemnt.member.adapter.web.growth.res.MemberUserPointsRes;
import com.lemnt.member.adapter.web.growth.res.MemberUserSignsRes;
import com.lemnt.member.application.growth.dto.AssetRecordDTO;
import com.lemnt.member.application.growth.dto.ConfigDTO;
import com.lemnt.member.application.growth.dto.LevelDTO;
import com.lemnt.member.application.growth.dto.LevelRecordDTO;
import com.lemnt.member.application.growth.dto.SignConfigDTO;
import com.lemnt.member.application.growth.dto.SignRecordDTO;

/** 会员成长权益转换器。 */
public final class MemberGrowthResConverter {

    private MemberGrowthResConverter() {}

    /**
     * 配置。
     */
    public static ConfigRes config(ConfigDTO v) {
        return v == null
                ? null
                : new ConfigRes(
                        v.pointTradeDeductEnable(),
                        v.pointTradeDeductUnitPrice(),
                        v.pointTradeDeductMaxPrice(),
                        v.pointTradeGivePoint());
    }

    /**
     * 等级记录。
     */
    public static MemberUserLevelRecordsRes toMemberUserLevelRecordsRes(LevelRecordDTO v) {
        return v == null
                ? null
                : new MemberUserLevelRecordsRes(
                        v.id(),
                        v.memberId(),
                        v.levelId(),
                        v.experience(),
                        v.reason(),
                        v.createTime());
    }

    /**
     * 等级记录。
     */
    public static MemberRecordLevelRecordsRes toMemberRecordLevelRecordsRes(LevelRecordDTO v) {
        return v == null
                ? null
                : new MemberRecordLevelRecordsRes(
                        v.id(),
                        v.memberId(),
                        v.levelId(),
                        v.experience(),
                        v.reason(),
                        v.createTime());
    }

    /**
     * 等级记录。
     */
    public static MemberRecordLevelRecordsDetailRes toMemberRecordLevelRecordsDetailRes(LevelRecordDTO v) {
        return v == null
                ? null
                : new MemberRecordLevelRecordsDetailRes(
                        v.id(),
                        v.memberId(),
                        v.levelId(),
                        v.experience(),
                        v.reason(),
                        v.createTime());
    }

    /**
     * 签到记录。
     */
    public static MemberUserSignsRes toMemberUserSignsRes(SignRecordDTO v) {
        return v == null
                ? null
                : new MemberUserSignsRes(
                        v.id(), v.memberId(), v.signDate(), v.day(), v.point(), v.experience());
    }

    /**
     * 签到记录。
     */
    public static MemberRecordSigninRecordsRes toMemberRecordSigninRecordsRes(SignRecordDTO v) {
        return v == null
                ? null
                : new MemberRecordSigninRecordsRes(
                        v.id(), v.memberId(), v.signDate(), v.day(), v.point(), v.experience());
    }

    /**
     * 资产记录。
     */
    public static MemberUserPointsRes toMemberUserPointsRes(AssetRecordDTO v) {
        return v == null
                ? null
                : new MemberUserPointsRes(
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

    /**
     * 资产记录。
     */
    public static MemberUserPointsRecordsRes toMemberUserPointsRecordsRes(AssetRecordDTO v) {
        return v == null
                ? null
                : new MemberUserPointsRecordsRes(
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

    /**
     * 资产记录。
     */
    public static MemberUserExperienceRes toMemberUserExperienceRes(AssetRecordDTO v) {
        return v == null
                ? null
                : new MemberUserExperienceRes(
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

    /**
     * 资产记录。
     */
    public static MemberUserExperienceRecordsRes toMemberUserExperienceRecordsRes(AssetRecordDTO v) {
        return v == null
                ? null
                : new MemberUserExperienceRecordsRes(
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

    /**
     * 资产记录。
     */
    public static MemberRecordPointRecordsRes toMemberRecordPointRecordsRes(AssetRecordDTO v) {
        return v == null
                ? null
                : new MemberRecordPointRecordsRes(
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

    /**
     * 资产记录。
     */
    public static MemberRecordPointRecordsDetailRes toMemberRecordPointRecordsDetailRes(AssetRecordDTO v) {
        return v == null
                ? null
                : new MemberRecordPointRecordsDetailRes(
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

    /**
     * 资产记录。
     */
    public static MemberRecordExperienceRecordsRes toMemberRecordExperienceRecordsRes(AssetRecordDTO v) {
        return v == null
                ? null
                : new MemberRecordExperienceRecordsRes(
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

    /**
     * 资产记录。
     */
    public static MemberRecordExperienceRecordsDetailRes toMemberRecordExperienceRecordsDetailRes(AssetRecordDTO v) {
        return v == null
                ? null
                : new MemberRecordExperienceRecordsDetailRes(
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

    /**
     * 等级。
     */
    public static MemberLevelListRes toMemberLevelListRes(LevelDTO v) {
        return v == null
                ? null
                : new MemberLevelListRes(
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
     * 等级。
     */
    public static MemberLevelDetailRes toMemberLevelDetailRes(LevelDTO v) {
        return v == null
                ? null
                : new MemberLevelDetailRes(
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
     * 等级。
     */
    public static MemberLevelSimpleRes toMemberLevelSimpleRes(LevelDTO v) {
        return v == null
                ? null
                : new MemberLevelSimpleRes(
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
    public static MemberConfigListRes toMemberConfigListRes(SignConfigDTO v) {
        return v == null ? null : new MemberConfigListRes(v.day(), v.point(), v.experience(), v.status());
    }

    /**
     * 签到配置。
     */
    public static MemberConfigDetailRes toMemberConfigDetailRes(SignConfigDTO v) {
        return v == null ? null : new MemberConfigDetailRes(v.day(), v.point(), v.experience(), v.status());
    }
}
