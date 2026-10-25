package com.lemnt.member.adapter.web.account.converter;

import com.lemnt.member.adapter.web.account.req.*;
import com.lemnt.member.application.account.cmd.*;
import com.lemnt.member.application.account.qry.*;

public final class MemberReqConverter {
    private MemberReqConverter() {}

    public static ProfileUpdateCmd profile(Long id, ProfileUpdateReq r) {
        return new ProfileUpdateCmd(
                id, r.nickname(), r.email(), r.avatar(), r.sex(), r.birthday(), null);
    }

    public static ProfileUpdateCmd profile(Long id, AdminProfileUpdateReq r) {
        return new ProfileUpdateCmd(
                id, r.nickname(), r.email(), r.avatar(), r.sex(), r.birthday(), r.status());
    }

    public static AddressSaveCmd address(Long memberId, Long id, AddressSaveReq r) {
        return new AddressSaveCmd(
                id,
                memberId,
                r.name(),
                r.mobile(),
                r.areaId(),
                r.detailAddress(),
                r.defaultStatus());
    }

    public static AssetAdjustCmd asset(Long id, String type, AssetAdjustReq r) {
        return new AssetAdjustCmd(id, type, r.delta(), r.requestNo(), r.reason());
    }

    public static LevelAssignCmd level(Long id, LevelAssignReq r) {
        return new LevelAssignCmd(id, r.levelId(), r.requestNo(), r.reason());
    }

    public static GroupAssignCmd group(Long id, GroupAssignReq r) {
        return new GroupAssignCmd(id, r.groupId());
    }

    public static TagsAssignCmd tags(Long id, TagsAssignReq r) {
        return new TagsAssignCmd(id, r.tagIds());
    }

    public static AuthLoginCmd login(AuthLoginReq r) {
        return new AuthLoginCmd(r.mobile(), r.password());
    }

    public static AuthRefreshCmd refresh(AuthRefreshReq r) {
        return new AuthRefreshCmd(r.refreshToken());
    }

    public static PasswordResetCmd password(Long id, PasswordResetReq r) {
        return new PasswordResetCmd(id, r.password());
    }

    public static PasswordChangeCmd password(Long id, PasswordChangeReq r) {
        return new PasswordChangeCmd(id, r.oldPassword(), r.newPassword());
    }

    public static com.lemnt.member.application.cmd.MemberRegisterCmd create(MemberCreateReq r) {
        return new com.lemnt.member.application.cmd.MemberRegisterCmd(
                r.nickname(), r.mobile(), r.email(), r.avatar());
    }

    private static int page(AccountPageReq r) {
        return r.pageNum() == null ? 1 : r.pageNum();
    }

    private static int size(AccountPageReq r) {
        return r.pageSize() == null ? 20 : r.pageSize();
    }

    public static ProfilesPageQry profiles(AccountPageReq r) {
        return new ProfilesPageQry(r.mobile(), page(r), size(r));
    }

    public static AssetsPageQry assets(Long id, String type, AccountPageReq r) {
        return new AssetsPageQry(id, type, page(r), size(r));
    }

    public static SignsPageQry signs(Long id, AccountPageReq r) {
        return new SignsPageQry(id, page(r), size(r));
    }

    public static LevelRecordsPageQry levelRecords(Long id, AccountPageReq r) {
        return new LevelRecordsPageQry(id, page(r), size(r));
    }

    public static LevelSaveCmd level(Long id, LevelSaveReq r) {
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

    public static GroupSaveCmd group(Long id, GroupSaveReq r) {
        return new GroupSaveCmd(id, r.name(), r.remark(), r.status());
    }

    public static TagSaveCmd tag(Long id, TagSaveReq r) {
        return new TagSaveCmd(id, r.name(), r.remark());
    }

    public static SignConfigSaveCmd signConfig(SignConfigSaveReq r) {
        return new SignConfigSaveCmd(r.day(), r.point(), r.experience(), r.status());
    }

    public static ConfigSaveCmd config(ConfigSaveReq r) {
        return new ConfigSaveCmd(
                r.pointTradeDeductEnable(),
                r.pointTradeDeductUnitPrice(),
                r.pointTradeDeductMaxPrice(),
                r.pointTradeGivePoint());
    }

    public static SmsSendCmd sms(Long id, SmsSendReq r) {
        return new SmsSendCmd(id, r.mobile(), r.scene());
    }

    public static SmsValidateCmd sms(Long id, SmsValidateReq r) {
        return new SmsValidateCmd(id, r.mobile(), r.scene(), r.code());
    }

    public static SmsLoginCmd sms(SmsLoginReq r) {
        return new SmsLoginCmd(r.mobile(), r.code());
    }

    public static SmsPasswordResetCmd password(SmsPasswordResetReq r) {
        return new SmsPasswordResetCmd(r.mobile(), r.code(), r.password());
    }

    public static MobileUpdateCmd mobile(Long id, MobileUpdateReq r) {
        return new MobileUpdateCmd(id, r.mobile(), r.code(), r.oldCode());
    }

    public static WechatMobileUpdateCmd mobile(Long id, WechatMobileUpdateReq r) {
        return new WechatMobileUpdateCmd(id, r.code());
    }

    public static SocialStateCmd state(Long id, SocialStateReq r) {
        return new SocialStateCmd(r.provider(), r.purpose(), id);
    }

    public static SocialLoginCmd social(SocialLoginReq r) {
        return new SocialLoginCmd(r.provider(), r.loginCode(), null, r.state());
    }

    public static SocialLoginCmd wechat(WechatLoginReq r) {
        return new SocialLoginCmd("WECHAT_MINI", r.loginCode(), r.phoneCode(), r.state());
    }

    public static SocialBindCmd bind(Long id, SocialBindReq r) {
        return new SocialBindCmd(id, r.provider(), r.code(), r.state());
    }

    public static SocialUnbindCmd unbind(Long id, SocialUnbindReq r) {
        return new SocialUnbindCmd(id, r.provider());
    }

    public static QrcodeCmd qrcode(QrcodeReq r) {
        return new QrcodeCmd(r.scene(), r.page());
    }

    public static JsapiCmd jsapi(JsapiReq r) {
        return new JsapiCmd(r.url());
    }

    public static com.lemnt.member.application.account.qry.AdminMemberPageQry adminPage(
            String kind, com.lemnt.member.adapter.web.account.req.AdminMemberPageReq req) {
        return new com.lemnt.member.application.account.qry.AdminMemberPageQry(
                kind,
                req.memberId(),
                req.name(),
                req.status(),
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }
}
