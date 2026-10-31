package com.lemnt.mall.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.promotion.converter.CombinationConverter;
import com.lemnt.mall.adapter.web.promotion.req.CombinationRecordPageReq;
import com.lemnt.mall.adapter.web.promotion.res.CombinationRecordRes;
import com.lemnt.mall.application.promotion.executor.CombinationRecordPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 拼团记录管理入口，仅供查询。 */
@RestController
@RequestMapping("/mall/promotion/combination-records")
@Tag(name = "拼团记录")
@RequiredArgsConstructor
public class CombinationRecordController {
    private final CombinationRecordPageQueryExe page;

    @GetMapping
    @Operation(summary = "拼团记录分页")
    @RequiresPermissions("mall:promotion:list")
    public R<CombinationRecordRes> page(@Valid @ModelAttribute CombinationRecordPageReq req) {
        var result = page.execute(CombinationConverter.recordPage(req));
        return R.page(result.getRecords().stream().map(CombinationConverter::record).toList(),
                result.getTotal());
    }
}
