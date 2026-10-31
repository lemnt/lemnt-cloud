package com.lemnt.member.adapter.web.user;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.user.req.MemberCatalogQueryGroupsPageReq;
import com.lemnt.member.adapter.web.user.converter.MemberReqConverter;
import com.lemnt.member.adapter.web.user.converter.MemberUserResConverter;
import com.lemnt.member.adapter.web.user.res.MemberCatalogQueryGroupsPageRes;
import com.lemnt.member.application.user.executor.MemberGroupPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/**
 * 后台会员记录与分页。
 */
@Tag(name = "会员分组分页")
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class MemberGroupQueryController {

    private final MemberGroupPageQueryExe memberGroupPageQueryExe;

    @GetMapping("/page")
    @Operation(summary = "groups/page分页")
    @RequiresPermissions("member:group:query")
    public R<MemberCatalogQueryGroupsPageRes> groupsPage(@Valid @ModelAttribute MemberCatalogQueryGroupsPageReq req) {
        var page = memberGroupPageQueryExe.execute(MemberReqConverter.toGroupPageQry(req));
        return R.page(
                page.getRecords().stream().map(MemberUserResConverter::toMemberCatalogQueryGroupsPageRes).toList(),
                page.getTotal());
    }
}
