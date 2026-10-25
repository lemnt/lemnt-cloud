package com.lemnt.member.adapter.web.account;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.account.req.*;
import com.lemnt.member.adapter.web.account.res.*;
import com.lemnt.member.adapter.web.account.converter.*;
import com.lemnt.member.application.account.qry.*;
import com.lemnt.member.application.account.executor.*;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "后台会员记录与分页")
@RestController
@RequestMapping("/member/admin")
@lombok.RequiredArgsConstructor
public class AdminMemberRecordController {
    private final MemberAdminQueryExe queries;

    @GetMapping("/groups/page")
    @Operation(summary = "groups/page分页")
    @RequiresPermissions("member:group:query")
    public R<AccountPageRes<GroupRes>> groups_page(@Valid @ModelAttribute AdminMemberPageReq req) {
        var page = queries.groups(MemberReqConverter.adminPage("GROUP", req));
        return R.ok(
                new AccountPageRes<>(
                        page.records().stream().map(MemberResConverter::group).toList(),
                        page.total()));
    }

    @GetMapping("/tags/page")
    @Operation(summary = "tags/page分页")
    @RequiresPermissions("member:tag:query")
    public R<AccountPageRes<TagRes>> tags_page(@Valid @ModelAttribute AdminMemberPageReq req) {
        var page = queries.tags(MemberReqConverter.adminPage("TAG", req));
        return R.ok(
                new AccountPageRes<>(
                        page.records().stream().map(MemberResConverter::tag).toList(),
                        page.total()));
    }

    @GetMapping("/point-records")
    @Operation(summary = "point-records分页")
    @RequiresPermissions("member:point:query")
    public R<AccountPageRes<AssetRecordRes>> point_records(
            @Valid @ModelAttribute AdminMemberPageReq req) {
        var page = queries.assets(MemberReqConverter.adminPage("POINT", req));
        return R.ok(
                new AccountPageRes<>(
                        page.records().stream().map(MemberResConverter::assetRecord).toList(),
                        page.total()));
    }

    @GetMapping("/point-records/{id}")
    @Operation(summary = "point-records详情")
    @RequiresPermissions("member:point:query")
    public R<AssetRecordRes> point_recordsDetail(@PathVariable Long id) {
        return R.ok(
                MemberResConverter.assetRecord(
                        queries.assetsDetail(new AdminMemberRecordDetailQry("POINT", id))));
    }

    @GetMapping("/experience-records")
    @Operation(summary = "experience-records分页")
    @RequiresPermissions("member:experience:query")
    public R<AccountPageRes<AssetRecordRes>> experience_records(
            @Valid @ModelAttribute AdminMemberPageReq req) {
        var page = queries.assets(MemberReqConverter.adminPage("EXPERIENCE", req));
        return R.ok(
                new AccountPageRes<>(
                        page.records().stream().map(MemberResConverter::assetRecord).toList(),
                        page.total()));
    }

    @GetMapping("/experience-records/{id}")
    @Operation(summary = "experience-records详情")
    @RequiresPermissions("member:experience:query")
    public R<AssetRecordRes> experience_recordsDetail(@PathVariable Long id) {
        return R.ok(
                MemberResConverter.assetRecord(
                        queries.assetsDetail(new AdminMemberRecordDetailQry("EXPERIENCE", id))));
    }

    @GetMapping("/level-records")
    @Operation(summary = "level-records分页")
    @RequiresPermissions("member:level:query")
    public R<AccountPageRes<LevelRecordRes>> level_records(
            @Valid @ModelAttribute AdminMemberPageReq req) {
        var page = queries.levels(MemberReqConverter.adminPage("LEVEL", req));
        return R.ok(
                new AccountPageRes<>(
                        page.records().stream().map(MemberResConverter::levelRecord).toList(),
                        page.total()));
    }

    @GetMapping("/level-records/{id}")
    @Operation(summary = "level-records详情")
    @RequiresPermissions("member:level:query")
    public R<LevelRecordRes> level_recordsDetail(@PathVariable Long id) {
        return R.ok(
                MemberResConverter.levelRecord(
                        queries.levelsDetail(new AdminMemberRecordDetailQry("LEVEL", id))));
    }

    @GetMapping("/signin-records")
    @Operation(summary = "signin-records分页")
    @RequiresPermissions("member:signin:query")
    public R<AccountPageRes<SignRecordRes>> signin_records(
            @Valid @ModelAttribute AdminMemberPageReq req) {
        var page = queries.signs(MemberReqConverter.adminPage("SIGN", req));
        return R.ok(
                new AccountPageRes<>(
                        page.records().stream().map(MemberResConverter::signRecord).toList(),
                        page.total()));
    }
}
