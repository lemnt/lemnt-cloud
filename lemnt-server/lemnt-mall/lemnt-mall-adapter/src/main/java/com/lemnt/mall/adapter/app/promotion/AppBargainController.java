package com.lemnt.mall.adapter.app.promotion;

import com.lemnt.mall.application.promotion.executor.PromotionOrderPlaceCmdExe;
import com.lemnt.mall.adapter.app.promotion.res.AppPromotionOrderRes;
import com.lemnt.mall.adapter.app.promotion.req.AppPromotionOrderCreateReq;
import com.lemnt.mall.adapter.app.promotion.converter.AppPromotionOrderConverter;
import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.mall.adapter.app.promotion.converter.AppBargainConverter;
import com.lemnt.mall.adapter.app.promotion.req.AppBargainHelpCreateReq;
import com.lemnt.mall.adapter.app.promotion.req.AppBargainRecordCreateReq;
import com.lemnt.mall.adapter.app.promotion.res.AppBargainActivityRes;
import com.lemnt.mall.adapter.app.promotion.res.AppBargainDetailRes;
import com.lemnt.mall.adapter.app.promotion.res.AppBargainHelpRes;
import com.lemnt.mall.adapter.app.promotion.res.AppBargainRecordRes;
import com.lemnt.mall.application.promotion.executor.BargainAppDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.BargainAppPageQueryExe;
import com.lemnt.mall.application.promotion.executor.BargainDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.BargainHelpCreateCmdExe;
import com.lemnt.mall.application.promotion.executor.BargainHelpPageQueryExe;
import com.lemnt.mall.application.promotion.executor.BargainLatestQueryExe;
import com.lemnt.mall.application.promotion.executor.BargainRecordCreateCmdExe;
import com.lemnt.mall.application.promotion.executor.BargainRecordPageQueryExe;
import com.lemnt.mall.application.promotion.qry.BargainHelpPageQry;
import com.lemnt.mall.application.promotion.qry.BargainRecordPageQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/** 小程序砍价入口。 */
@Tag(name = "小程序砍价")
@RestController
@RequestMapping("/mall/app/bargain")
@RequiredArgsConstructor
public class AppBargainController {
    private final BargainAppPageQueryExe page;
    private final BargainAppDetailQueryExe detail;
    private final BargainLatestQueryExe latest;
    private final BargainRecordCreateCmdExe create;
    private final BargainHelpCreateCmdExe help;
    private final BargainRecordPageQueryExe records;
    private final BargainDetailQueryExe recordDetail;
    private final PromotionOrderPlaceCmdExe orderPlace;
    private final BargainHelpPageQueryExe helps;

    @GetMapping("/activities")
    @Operation(summary = "砍价活动分页")
    public R<AppBargainActivityRes> activities(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        var result = page.execute(pageNum, pageSize);
        return R.page(result.getRecords().stream().map(AppBargainConverter::activity).toList(),
                result.getTotal());
    }

    @GetMapping("/activities/{id}")
    @Operation(summary = "砍价活动详情")
    public R<AppBargainActivityRes> activity(@PathVariable Long id) {
        return R.ok(AppBargainConverter.activity(detail.execute(id)));
    }

    @GetMapping("/activities/{id}/records")
    @Operation(summary = "活动最近砍价记录")
    public R<List<AppBargainRecordRes>> activityRecords(@PathVariable Long id,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "20") Integer count) {
        return R.ok(latest.execute(id, status, count).stream().map(AppBargainConverter::record).toList());
    }

    @PostMapping("/records")
    @Operation(summary = "发起砍价")
    public R<Long> create(@Valid @RequestBody AppBargainRecordCreateReq req) {
        return R.ok(create.execute(SecurityUtils.getUserId(), req.activityId()));
    }

    @GetMapping("/records")
    @Operation(summary = "我的砍价记录分页")
    public R<AppBargainRecordRes> records(@RequestParam(required = false) Long activityId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        var result = records.execute(new BargainRecordPageQry(SecurityUtils.getUserId(), activityId,
                status, pageNum, pageSize));
        return R.page(result.getRecords().stream().map(AppBargainConverter::record).toList(),
                result.getTotal());
    }

    @GetMapping("/records/{id}")
    @Operation(summary = "砍价详情")
    public R<AppBargainDetailRes> record(@PathVariable Long id) {
        return R.ok(AppBargainConverter.detail(recordDetail.execute(id, SecurityUtils.getUserId())));
    }

    @GetMapping("/records/{id}/helps")
    @Operation(summary = "砍价助力列表")
    public R<AppBargainHelpRes> recordHelps(@PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        var result = helps.execute(new BargainHelpPageQry(id, null, null, pageNum, pageSize));
        return R.page(result.getRecords().stream().map(AppBargainConverter::help).toList(),
                result.getTotal());
    }

    @PostMapping("/helps")
    @Operation(summary = "帮好友砍一刀")
    public R<AppBargainHelpRes> help(@Valid @RequestBody AppBargainHelpCreateReq req) {
        return R.ok(AppBargainConverter.help(help.execute(SecurityUtils.getUserId(), req.recordId())));
    }

    @PostMapping("/orders")
    @Operation(summary = "砍价成功后下单")
    public R<AppPromotionOrderRes> order(@Valid @RequestBody AppPromotionOrderCreateReq req) {
        return R.ok(AppPromotionOrderConverter.res(orderPlace.execute(
                AppPromotionOrderConverter.cmd(SecurityUtils.getUserId(), "BARGAIN", req))));
    }
}
