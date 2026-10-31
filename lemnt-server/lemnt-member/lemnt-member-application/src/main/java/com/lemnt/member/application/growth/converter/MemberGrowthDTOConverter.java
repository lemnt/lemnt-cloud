package com.lemnt.member.application.growth.converter;

import com.lemnt.member.application.growth.dto.AssetRecordDTO;
import com.lemnt.member.application.growth.dto.ConfigDTO;
import com.lemnt.member.application.growth.dto.LevelDTO;
import com.lemnt.member.application.growth.dto.LevelRecordDTO;
import com.lemnt.member.application.growth.dto.SignConfigDTO;
import com.lemnt.member.application.growth.dto.SignRecordDTO;
import com.lemnt.member.domain.growth.model.AssetRecord;
import com.lemnt.member.domain.growth.model.Config;
import com.lemnt.member.domain.growth.model.Level;
import com.lemnt.member.domain.growth.model.LevelRecord;
import com.lemnt.member.domain.growth.model.SignConfig;
import com.lemnt.member.domain.growth.model.SignRecord;

/** 会员成长权益转换器。 */
public final class MemberGrowthDTOConverter {

    private MemberGrowthDTOConverter() {}

    /**
     * 资产记录。
     */
    public static AssetRecordDTO toAssetRecordDTO(AssetRecord v) {
        return v == null
                ? null
                : new AssetRecordDTO(
                        v.id(),
                        v.memberId(),
                        v.assetType(),
                        v.delta(),
                        v.balance(),
                        v.bizType(),
                        v.bizNo(),
                        v.reason(),
                        v.createTime(),v.nickname(),v.pointBizType(),v.title(),v.description(),v.experienceBizType());
    }

    /**
     * 等级。
     */
    public static LevelDTO toLevelDTO(Level v) {
        return v == null
                ? null
                : new LevelDTO(
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
    public static SignConfigDTO toSignConfigDTO(SignConfig v) {
        return v == null ? null : new SignConfigDTO(v.id(), v.day(), v.point(), v.experience(), v.status());
    }

    /**
     * 签到记录。
     */
    public static SignRecordDTO toSignRecordDTO(SignRecord v) {
        return v == null
                ? null
                : new SignRecordDTO(
                        v.id(), v.memberId(), v.signDate(), v.day(), v.point(), v.experience(), v.nickname(), v.createTime());
    }

    /**
     * 等级记录。
     */
    public static LevelRecordDTO toLevelRecordDTO(LevelRecord v) {
        return v == null
                ? null
                : new LevelRecordDTO(
                        v.id(),
                        v.memberId(),
                        v.levelId(),
                        v.experience(),
                        v.reason(),
                        v.createTime(),
                        v.level(),
                        v.discountPercent(),
                        v.recordExperience(),
                        v.description());
    }

    /**
     * 配置。
     */
    public static ConfigDTO toConfigDTO(Config v) {
        return v == null
                ? null
                : new ConfigDTO(
                        v.pointTradeDeductEnable(),
                        v.pointTradeDeductUnitPrice(),
                        v.pointTradeDeductMaxPrice(),
                        v.pointTradeGivePoint());
    }
}
