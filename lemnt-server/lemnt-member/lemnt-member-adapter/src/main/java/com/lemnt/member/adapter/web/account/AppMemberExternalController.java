package com.lemnt.member.adapter.web.account;

import com.lemnt.common.core.domain.R;
import com.lemnt.member.adapter.web.account.req.*;
import com.lemnt.member.adapter.web.account.res.*;
import com.lemnt.member.adapter.web.account.converter.*;
import com.lemnt.member.application.account.qry.*;
import com.lemnt.member.application.account.executor.*;
import com.lemnt.member.application.account.service.MemberAuthService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Tag(name = "会员短信与微信认证")
@RestController
@RequestMapping("/member/app")
@RequiredArgsConstructor
public class AppMemberExternalController {
    private final MemberAuthService auth;
    private final MemberExternalCmdExe commands;
    private final MemberExternalQueryExe queries;

    private Long smsOwner(String token, String scene) {
        return "UPDATE_OLD_MOBILE".equals(scene) || "UPDATE_NEW_MOBILE".equals(scene)
                ? auth.require(token)
                : null;
    }

    @PostMapping("/auth/send-sms-code")
    @Operation(summary = "发送场景验证码，默认未配置时拒绝，日额度及频率受限")
    public R<Boolean> send(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody SmsSendReq req) {
        return R.ok(commands.execute(MemberReqConverter.sms(smsOwner(token, req.scene()), req)));
    }

    @PostMapping("/auth/validate-sms-code")
    @Operation(summary = "验证验证码但不消费，错误次数受限")
    public R<Boolean> validate(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody SmsValidateReq req) {
        return R.ok(commands.execute(MemberReqConverter.sms(smsOwner(token, req.scene()), req)));
    }

    @PostMapping("/auth/sms-login")
    @Operation(summary = "消费LOGIN验证码并登录，首次验证手机号后创建会员")
    public R<TokenPairRes> login(@Valid @RequestBody SmsLoginReq req) {
        return R.ok(MemberResConverter.tokenPair(commands.execute(MemberReqConverter.sms(req))));
    }

    @PutMapping("/user/reset-password")
    @Operation(summary = "消费RESET_PASSWORD验证码并重置密码，撤销全部会话")
    public R<Boolean> reset(@Valid @RequestBody SmsPasswordResetReq req) {
        return R.ok(commands.execute(MemberReqConverter.password(req)));
    }

    @PutMapping("/user/mobile")
    @Operation(summary = "验证原手机和新手机验证码后修改手机号，撤销全部会话")
    public R<Boolean> mobile(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody MobileUpdateReq req) {
        return R.ok(commands.execute(MemberReqConverter.mobile(auth.require(token), req)));
    }

    @PutMapping("/user/mobile-by-weixin")
    @Operation(summary = "服务端兑换微信手机code后修改，不接受明文手机号")
    public R<Boolean> wechatMobile(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody WechatMobileUpdateReq req) {
        return R.ok(commands.execute(MemberReqConverter.mobile(auth.require(token), req)));
    }

    @PostMapping("/auth/social-state")
    @Operation(summary = "生成绑定或登录用的一次性state，BIND必须当前会员登录")
    public R<SocialStateRes> state(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody SocialStateReq req) {
        Long id = "BIND".equals(req.purpose()) ? auth.require(token) : null;
        return R.ok(
                MemberResConverter.socialState(
                        commands.execute(MemberReqConverter.state(id, req))));
    }

    @GetMapping("/auth/social-auth-redirect")
    @Operation(summary = "公众号登录授权地址，固定服务端回调，返回state")
    public R<SocialStateRes> redirect() {
        return R.ok(
                MemberResConverter.socialState(
                        commands.execute(
                                new com.lemnt.member.application.account.cmd.SocialStateCmd(
                                        "WECHAT_MP", "LOGIN", null))));
    }

    @PostMapping("/auth/social-login")
    @Operation(summary = "服务器兑换第三方code和一次性state后登录，只支持配置的微信提供方")
    public R<TokenPairRes> social(@Valid @RequestBody SocialLoginReq req) {
        return R.ok(MemberResConverter.tokenPair(commands.execute(MemberReqConverter.social(req))));
    }

    @PostMapping("/auth/weixin-mini-app-login")
    @Operation(summary = "服务器校验微信登录code和手机号code后登录")
    public R<TokenPairRes> wechat(@Valid @RequestBody WechatLoginReq req) {
        return R.ok(MemberResConverter.tokenPair(commands.execute(MemberReqConverter.wechat(req))));
    }

    @PostMapping("/auth/create-weixin-jsapi-signature")
    @Operation(summary = "允许域名内的公众号JSAPI签名，不返回公众号密钥")
    public R<JsapiConfigRes> jsapi(@Valid @RequestBody JsapiReq req) {
        return R.ok(
                MemberResConverter.jsapiConfig(commands.execute(MemberReqConverter.jsapi(req))));
    }

    @GetMapping("/social")
    @Operation(summary = "本人已绑定的提供方名称，不返回openid/session_key")
    public R<List<String>> bindings(
            @RequestHeader(value = "Authorization", required = false) String token) {
        return R.ok(queries.execute(new SocialBindingsQry(auth.require(token))));
    }

    @PostMapping("/social/bind")
    @Operation(summary = "服务端验证code和绑定state，禁止绑定他人的第三方身份")
    public R<Boolean> bind(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody SocialBindReq req) {
        return R.ok(commands.execute(MemberReqConverter.bind(auth.require(token), req)));
    }

    @DeleteMapping("/social/bind")
    @Operation(summary = "解绑本人身份，最后一种登录方式不可解绑")
    public R<Boolean> unbind(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody SocialUnbindReq req) {
        return R.ok(commands.execute(MemberReqConverter.unbind(auth.require(token), req)));
    }

    @PostMapping("/social/wxa-qrcode")
    @Operation(summary = "生成正式版小程序码，返回PNG base64")
    public R<QrcodeRes> qrcode(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody QrcodeReq req) {
        auth.require(token);
        return R.ok(MemberResConverter.qrcode(commands.execute(MemberReqConverter.qrcode(req))));
    }

    @GetMapping("/social/subscribe-template-list")
    @Operation(summary = "配置小程序的订阅消息模板列表")
    public R<List<SubscribeTemplateRes>> templates(
            @RequestHeader(value = "Authorization", required = false) String token) {
        auth.require(token);
        return R.ok(
                queries.execute(new SubscribeTemplatesQry()).stream()
                        .map(MemberResConverter::subscribeTemplate)
                        .toList());
    }
}
