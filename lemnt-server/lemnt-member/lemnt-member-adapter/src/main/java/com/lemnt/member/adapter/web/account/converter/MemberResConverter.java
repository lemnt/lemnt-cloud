package com.lemnt.member.adapter.web.account.converter;

import com.lemnt.member.application.account.dto.*;
import com.lemnt.member.adapter.web.account.res.*;

public final class MemberResConverter {
    private MemberResConverter() {}

    public static ProfileRes profile(ProfileDTO v) {
        return v == null
                ? null
                : new ProfileRes(
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

    public static AddressRes address(AddressDTO v) {
        return v == null
                ? null
                : new AddressRes(
                        v.id(),
                        v.memberId(),
                        v.name(),
                        v.mobile(),
                        v.areaId(),
                        v.detailAddress(),
                        v.defaultStatus());
    }

    public static AssetRecordRes assetRecord(AssetRecordDTO v) {
        return v == null
                ? null
                : new AssetRecordRes(
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

    public static GroupRes group(GroupDTO v) {
        return v == null ? null : new GroupRes(v.id(), v.name(), v.remark(), v.status());
    }

    public static TagRes tag(TagDTO v) {
        return v == null ? null : new TagRes(v.id(), v.name(), v.remark());
    }

    public static SignConfigRes signConfig(SignConfigDTO v) {
        return v == null ? null : new SignConfigRes(v.day(), v.point(), v.experience(), v.status());
    }

    public static SignRecordRes signRecord(SignRecordDTO v) {
        return v == null
                ? null
                : new SignRecordRes(
                        v.id(), v.memberId(), v.signDate(), v.day(), v.point(), v.experience());
    }

    public static LevelRecordRes levelRecord(LevelRecordDTO v) {
        return v == null
                ? null
                : new LevelRecordRes(
                        v.id(),
                        v.memberId(),
                        v.levelId(),
                        v.experience(),
                        v.reason(),
                        v.createTime());
    }

    public static ConfigRes config(ConfigDTO v) {
        return v == null
                ? null
                : new ConfigRes(
                        v.pointTradeDeductEnable(),
                        v.pointTradeDeductUnitPrice(),
                        v.pointTradeDeductMaxPrice(),
                        v.pointTradeGivePoint());
    }

    public static TokenPairRes tokenPair(TokenPairDTO v) {
        return v == null
                ? null
                : new TokenPairRes(v.memberId(), v.accessToken(), v.refreshToken(), v.expiresIn());
    }

    public static SignSummaryRes summary(
            com.lemnt.member.application.account.dto.SignSummaryDTO v) {
        return new SignSummaryRes(v.totalDay(), v.continuousDay(), v.todaySignIn());
    }

    public static <D, T> AccountPageRes<T> page(
            com.lemnt.member.application.account.dto.AccountPageDTO<D> v,
            java.util.function.Function<D, T> converter) {
        return new AccountPageRes<>(v.records().stream().map(converter).toList(), v.total());
    }

    public static SocialStateRes socialState(SocialStateDTO v) {
        return new SocialStateRes(v.state(), v.authorizationUrl());
    }

    public static JsapiConfigRes jsapiConfig(JsapiConfigDTO v) {
        return new JsapiConfigRes(v.appId(), v.nonceStr(), v.timestamp(), v.signature());
    }

    public static SubscribeTemplateRes subscribeTemplate(SubscribeTemplateDTO v) {
        return new SubscribeTemplateRes(v.id(), v.title(), v.content(), v.type());
    }

    public static QrcodeRes qrcode(QrcodeDTO v) {
        return new QrcodeRes(v.base64(), v.contentType());
    }
}
