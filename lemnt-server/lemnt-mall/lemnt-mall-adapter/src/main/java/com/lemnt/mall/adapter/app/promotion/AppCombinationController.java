package com.lemnt.mall.adapter.app.promotion;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import com.lemnt.mall.application.promotion.executor.PromotionOrderPlaceCmdExe;
import com.lemnt.mall.adapter.app.promotion.res.AppPromotionOrderRes;
import com.lemnt.mall.adapter.app.promotion.req.AppPromotionOrderCreateReq;
import com.lemnt.mall.adapter.app.promotion.converter.AppPromotionOrderConverter;
import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.mall.adapter.app.promotion.converter.AppCombinationConverter;
import com.lemnt.mall.adapter.app.promotion.res.AppCombinationActivityRes;
import com.lemnt.mall.adapter.app.promotion.res.AppCombinationDetailRes;
import com.lemnt.mall.adapter.app.promotion.res.AppCombinationRecordDetailRes;
import com.lemnt.mall.adapter.app.promotion.res.AppCombinationRecordRes;
import com.lemnt.mall.adapter.app.promotion.res.AppCombinationSummaryRes;
import com.lemnt.mall.application.promotion.executor.CombinationAppDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.CombinationAppPageQueryExe;
import com.lemnt.mall.application.promotion.executor.CombinationHeadListQueryExe;
import com.lemnt.mall.application.promotion.executor.CombinationRecordDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.CombinationRecordPageQueryExe;
import com.lemnt.mall.application.promotion.executor.CombinationSuccessCountQueryExe;
import com.lemnt.mall.application.promotion.executor.CombinationSummaryQueryExe;
import com.lemnt.mall.application.promotion.qry.CombinationRecordPageQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import java.util.List;

/** 小程序拼团展示入口。 */
@Tag(name = "小程序拼团")
@RestController
@RequestMapping("/mall/app/combination")
@RequiredArgsConstructor
public class AppCombinationController {
    private final CombinationAppPageQueryExe page;
    private final CombinationAppDetailQueryExe detail;
    private final CombinationHeadListQueryExe heads;
    private final CombinationRecordPageQueryExe records;
    private final CombinationRecordDetailQueryExe recordDetail;
    private final CombinationSummaryQueryExe summary;
    private final PromotionOrderPlaceCmdExe orderPlace;
    private final CombinationSuccessCountQueryExe successCount;

    @GetMapping("/activities")
    @Operation(summary = "拼团活动分页")
    public R<AppCombinationActivityRes> activities(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        var result = page.execute(pageNum, pageSize);
        return R.page(result.getRecords().stream().map(AppCombinationConverter::activity).toList(),
                result.getTotal());
    }

    @GetMapping("/activities/{id}")
    @Operation(summary = "拼团活动详情")
    public R<AppCombinationDetailRes> activity(@PathVariable Long id) {
        return R.ok(AppCombinationConverter.detail(detail.execute(id), successCount.execute(id)));
    }

    @GetMapping("/activities/{id}/heads")
    @Operation(summary = "可参与的团长记录")
    public R<List<AppCombinationRecordRes>> heads(@PathVariable Long id,
            @RequestParam(defaultValue = "0") Integer status,
            @RequestParam(defaultValue = "20") Integer count) {
        return R.ok(heads.execute(id, status, count).stream().map(AppCombinationConverter::record).toList());
    }

    @GetMapping("/records")
    @Operation(summary = "我的拼团记录分页")
    public R<AppCombinationRecordRes> records(@RequestParam(required = false) Long activityId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        var result = records.execute(new CombinationRecordPageQry(SecurityUtils.getUserId(), activityId,
                status, null, pageNum, pageSize));
        return R.page(result.getRecords().stream().map(AppCombinationConverter::record).toList(),
                result.getTotal());
    }

    @GetMapping("/records/{id}")
    @Operation(summary = "拼团记录详情")
    public R<AppCombinationRecordDetailRes> record(@PathVariable Long id) {
        return R.ok(AppCombinationConverter.recordDetail(recordDetail.execute(id,
                SecurityUtils.getUserId())));
    }

    @GetMapping("/summary")
    @Operation(summary = "拼团概要")
    public R<AppCombinationSummaryRes> summary() {
        return R.ok(AppCombinationConverter.summary(summary.execute()));
    }

    @PostMapping("/orders")
    @Operation(summary = "拼团活动下单")
    public R<AppPromotionOrderRes> order(@Valid @RequestBody AppPromotionOrderCreateReq req) {
        return R.ok(AppPromotionOrderConverter.res(orderPlace.execute(
                AppPromotionOrderConverter.cmd(SecurityUtils.getUserId(), "COMBINATION", req))));
    }
}
