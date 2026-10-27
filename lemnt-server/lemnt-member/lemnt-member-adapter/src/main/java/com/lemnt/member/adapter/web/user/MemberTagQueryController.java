package com.lemnt.member.adapter.web.user;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.growth.req.MemberCatalogQueryTagsPageReq;
import com.lemnt.member.adapter.web.user.converter.MemberReqConverter;
import com.lemnt.member.adapter.web.user.converter.MemberUserResConverter;
import com.lemnt.member.adapter.web.user.res.MemberCatalogQueryTagsPageRes;
import com.lemnt.member.application.user.executor.MemberTagPageQueryExe;

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
@Tag(name = "会员标签分页")
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class MemberTagQueryController {

    private final MemberTagPageQueryExe memberTagPageQueryExe;

    @GetMapping("/tags/page")
    @Operation(summary = "tags/page分页")
    @RequiresPermissions("member:tag:query")
    public R<MemberCatalogQueryTagsPageRes> tagsPage(@Valid @ModelAttribute MemberCatalogQueryTagsPageReq req) {
        var page = memberTagPageQueryExe.execute(MemberReqConverter.toTagPageQry(req));
        return R.page(
                page.getRecords().stream().map(MemberUserResConverter::toMemberCatalogQueryTagsPageRes).toList(),
                page.getTotal());
    }
}
