package com.lemnt.member.adapter.web.account;

import com.lemnt.common.core.domain.R;
import com.lemnt.member.adapter.web.account.req.*;
import com.lemnt.member.adapter.web.account.res.*;
import com.lemnt.member.adapter.web.account.converter.*;
import com.lemnt.member.application.account.cmd.*;
import com.lemnt.member.application.account.qry.*;
import com.lemnt.member.application.account.executor.*;
import com.lemnt.member.application.account.service.MemberAuthService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Tag(name = "小程序会员资料与权益")
@RestController
@RequestMapping("/member/app")
@RequiredArgsConstructor
public class AppMemberAccountController {
    private final MemberAuthService auth;
    private final MemberAccountCmdExe commands;
    private final MemberAccountQueryExe queries;
    private final MemberAuthCmdExe authentication;

    @GetMapping("/user")
    @Operation(summary = "当前会员资料")
    public R<ProfileRes> user(
            @RequestHeader(value = "Authorization", required = false) String token) {
        return R.ok(
                MemberResConverter.profile(queries.execute(new ProfileQry(auth.require(token)))));
    }

    @PutMapping("/user")
    @Operation(summary = "更新本人资料，不接受会员ID、手机号、积分、等级或状态")
    public R<Boolean> update(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody ProfileUpdateReq req) {
        return R.ok(commands.execute(MemberReqConverter.profile(auth.require(token), req)));
    }

    @PutMapping("/user/password")
    @Operation(summary = "验证原密码并修改，撤销全部会员会话")
    public R<Boolean> password(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody PasswordChangeReq req) {
        return R.ok(authentication.execute(MemberReqConverter.password(auth.require(token), req)));
    }

    @GetMapping("/levels")
    @Operation(summary = "已启用会员等级")
    public R<List<LevelRes>> levels(
            @RequestHeader(value = "Authorization", required = false) String token) {
        auth.require(token);
        return R.ok(
                queries.execute(new LevelsQry(true)).stream()
                        .map(MemberResConverter::level)
                        .toList());
    }

    @GetMapping("/signin/config")
    @Operation(summary = "已启用签到规则")
    public R<List<SignConfigRes>> signConfig(
            @RequestHeader(value = "Authorization", required = false) String token) {
        auth.require(token);
        return R.ok(
                queries.execute(new SignConfigsQry(true)).stream()
                        .map(MemberResConverter::signConfig)
                        .toList());
    }

    @GetMapping("/signin/summary")
    @Operation(summary = "本人签到汇总")
    public R<SignSummaryRes> summary(
            @RequestHeader(value = "Authorization", required = false) String token) {
        return R.ok(
                MemberResConverter.summary(
                        queries.execute(new SignSummaryQry(auth.require(token)))));
    }

    @PostMapping("/signin")
    @Operation(summary = "数据库日期每日幂等签到，积分与经验同事务发放")
    public R<SignRecordRes> sign(
            @RequestHeader(value = "Authorization", required = false) String token) {
        return R.ok(
                MemberResConverter.signRecord(
                        commands.execute(new SignCreateCmd(auth.require(token)))));
    }

    @GetMapping("/signin/records")
    @Operation(summary = "本人签到记录分页")
    public R<AccountPageRes<SignRecordRes>> signs(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @ModelAttribute AccountPageReq req) {
        return R.ok(
                MemberResConverter.page(
                        queries.execute(MemberReqConverter.signs(auth.require(token), req)),
                        MemberResConverter::signRecord));
    }

    @GetMapping("/point-records")
    @Operation(summary = "本人POINT流水分页")
    public R<AccountPageRes<AssetRecordRes>> point(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @ModelAttribute AccountPageReq req) {
        return R.ok(
                MemberResConverter.page(
                        queries.execute(
                                MemberReqConverter.assets(auth.require(token), "POINT", req)),
                        MemberResConverter::assetRecord));
    }

    @GetMapping("/experience-records")
    @Operation(summary = "本人EXPERIENCE流水分页")
    public R<AccountPageRes<AssetRecordRes>> experience(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @ModelAttribute AccountPageReq req) {
        return R.ok(
                MemberResConverter.page(
                        queries.execute(
                                MemberReqConverter.assets(auth.require(token), "EXPERIENCE", req)),
                        MemberResConverter::assetRecord));
    }
}
