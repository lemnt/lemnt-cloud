package com.lemnt.member.application.account.converter;

import com.lemnt.member.domain.account.model.*;
import com.lemnt.member.application.account.dto.*;

public final class MemberDTOConverter {
    private MemberDTOConverter() {}

    public static ProfileDTO profile(Profile v) {
        return v == null
                ? null
                : new ProfileDTO(
                        v.id(),
                        v.memberNo(),
                        v.nickname(),
                        v.mobile(),
                        v.email(),
                        v.avatar(),
                        v.status(),
                        v.points(),
                        v.experience(),
                        v.levelId(),
                        v.groupId(),
                        v.sex(),
                        v.birthday(),
                        v.createTime());
    }

    public static AddressDTO address(Address v) {
        return v == null
                ? null
                : new AddressDTO(
                        v.id(),
                        v.memberId(),
                        v.name(),
                        v.mobile(),
                        v.areaId(),
                        v.detailAddress(),
                        v.defaultStatus());
    }

    public static AssetRecordDTO assetRecord(AssetRecord v) {
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
                        v.createTime());
    }

    public static LevelDTO level(Level v) {
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

    public static GroupDTO group(Group v) {
        return v == null ? null : new GroupDTO(v.id(), v.name(), v.remark(), v.status());
    }

    public static TagDTO tag(Tag v) {
        return v == null ? null : new TagDTO(v.id(), v.name(), v.remark());
    }

    public static SignConfigDTO signConfig(SignConfig v) {
        return v == null ? null : new SignConfigDTO(v.day(), v.point(), v.experience(), v.status());
    }

    public static SignRecordDTO signRecord(SignRecord v) {
        return v == null
                ? null
                : new SignRecordDTO(
                        v.id(), v.memberId(), v.signDate(), v.day(), v.point(), v.experience());
    }

    public static LevelRecordDTO levelRecord(LevelRecord v) {
        return v == null
                ? null
                : new LevelRecordDTO(
                        v.id(),
                        v.memberId(),
                        v.levelId(),
                        v.experience(),
                        v.reason(),
                        v.createTime());
    }

    public static ConfigDTO config(Config v) {
        return v == null
                ? null
                : new ConfigDTO(
                        v.pointTradeDeductEnable(),
                        v.pointTradeDeductUnitPrice(),
                        v.pointTradeDeductMaxPrice(),
                        v.pointTradeGivePoint());
    }

    public static TokenPairDTO tokenPair(TokenPair v) {
        return v == null
                ? null
                : new TokenPairDTO(v.memberId(), v.accessToken(), v.refreshToken(), v.expiresIn());
    }
}
