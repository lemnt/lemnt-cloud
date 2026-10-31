package com.lemnt.mall.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.promotion.converter.BargainConverter;
import com.lemnt.mall.adapter.web.promotion.req.BargainHelpPageReq;
import com.lemnt.mall.adapter.web.promotion.req.BargainRecordPageReq;
import com.lemnt.mall.adapter.web.promotion.res.BargainHelpRes;
import com.lemnt.mall.adapter.web.promotion.res.BargainRecordRes;
import com.lemnt.mall.application.promotion.executor.BargainHelpPageQueryExe;
import com.lemnt.mall.application.promotion.executor.BargainRecordPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 砍价记录与助力管理入口，仅供查询。 */
@RestController
@RequestMapping("/mall/promotion")
@Tag(name = "砍价记录")
@RequiredArgsConstructor
public class BargainRecordController {
    private final BargainRecordPageQueryExe records;
    private final BargainHelpPageQueryExe helps;

    @GetMapping("/bargain-records")
    @Operation(summary = "砍价记录分页")
    @RequiresPermissions("mall:promotion:list")
    public R<BargainRecordRes> records(@Valid @ModelAttribute BargainRecordPageReq req) {
        var result = records.execute(BargainConverter.recordPage(req));
        return R.page(result.getRecords().stream().map(BargainConverter::record).toList(),
                result.getTotal());
    }

    @GetMapping("/bargain-helps")
    @Operation(summary = "砍价助力分页")
    @RequiresPermissions("mall:promotion:list")
    public R<BargainHelpRes> helps(@Valid @ModelAttribute BargainHelpPageReq req) {
        var result = helps.execute(BargainConverter.helpPage(req));
        return R.page(result.getRecords().stream().map(BargainConverter::help).toList(),
                result.getTotal());
    }
}
