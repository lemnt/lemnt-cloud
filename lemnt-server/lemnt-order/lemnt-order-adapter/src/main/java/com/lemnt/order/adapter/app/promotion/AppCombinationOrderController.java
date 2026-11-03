package com.lemnt.order.adapter.app.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.order.adapter.app.promotion.converter.AppCombinationConverter;
import com.lemnt.order.adapter.app.promotion.converter.AppPromotionOrderConverter;
import com.lemnt.order.adapter.app.promotion.req.AppPromotionOrderCreateReq;
import com.lemnt.order.adapter.app.promotion.res.AppCombinationActivityRes;
import com.lemnt.order.adapter.app.promotion.res.AppCombinationDetailRes;
import com.lemnt.order.adapter.app.promotion.res.AppCombinationRecordDetailRes;
import com.lemnt.order.adapter.app.promotion.res.AppCombinationRecordRes;
import com.lemnt.order.adapter.app.promotion.res.AppCombinationSummaryRes;
import com.lemnt.order.adapter.app.promotion.res.AppPromotionOrderRes;
import com.lemnt.order.application.promotion.executor.CombinationAppDetailQueryExe;
import com.lemnt.order.application.promotion.executor.CombinationAppPageQueryExe;
import com.lemnt.order.application.promotion.executor.CombinationHeadListQueryExe;
import com.lemnt.order.application.promotion.executor.CombinationRecordDetailQueryExe;
import com.lemnt.order.application.promotion.executor.CombinationRecordPageQueryExe;
import com.lemnt.order.application.promotion.executor.CombinationSuccessCountQueryExe;
import com.lemnt.order.application.promotion.executor.CombinationSummaryQueryExe;
import com.lemnt.order.application.promotion.executor.PromotionOrderPlaceCmdExe;
import com.lemnt.order.application.promotion.qry.CombinationRecordPageQry;

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

/** 原活动下单入口，统一由订单服务执行。 */
@Tag(name="活动订单")
@RestController
@RequestMapping("/mall/app/combination")
@RequiredArgsConstructor
public class AppCombinationOrderController {
    private final PromotionOrderPlaceCmdExe orderPlace;
    @PostMapping("/orders")
    @Operation(summary = "拼团活动下单")
    public R<AppPromotionOrderRes> order(@Valid @RequestBody AppPromotionOrderCreateReq req) {
        return R.ok(
                AppPromotionOrderConverter.res(
                        orderPlace.execute(
                                AppPromotionOrderConverter.cmd(
                                        SecurityUtils.getUserId(), "COMBINATION", req))));
    }
}
