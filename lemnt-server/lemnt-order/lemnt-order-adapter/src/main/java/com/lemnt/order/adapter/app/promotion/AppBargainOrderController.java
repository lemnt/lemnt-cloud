package com.lemnt.order.adapter.app.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.order.adapter.app.promotion.converter.AppBargainConverter;
import com.lemnt.order.adapter.app.promotion.converter.AppPromotionOrderConverter;
import com.lemnt.order.adapter.app.promotion.req.AppBargainHelpCreateReq;
import com.lemnt.order.adapter.app.promotion.req.AppBargainRecordCreateReq;
import com.lemnt.order.adapter.app.promotion.req.AppPromotionOrderCreateReq;
import com.lemnt.order.adapter.app.promotion.res.AppBargainActivityRes;
import com.lemnt.order.adapter.app.promotion.res.AppBargainDetailRes;
import com.lemnt.order.adapter.app.promotion.res.AppBargainHelpRes;
import com.lemnt.order.adapter.app.promotion.res.AppBargainRecordRes;
import com.lemnt.order.adapter.app.promotion.res.AppPromotionOrderRes;
import com.lemnt.order.application.promotion.executor.BargainAppDetailQueryExe;
import com.lemnt.order.application.promotion.executor.BargainAppPageQueryExe;
import com.lemnt.order.application.promotion.executor.BargainDetailQueryExe;
import com.lemnt.order.application.promotion.executor.BargainHelpCreateCmdExe;
import com.lemnt.order.application.promotion.executor.BargainHelpPageQueryExe;
import com.lemnt.order.application.promotion.executor.BargainLatestQueryExe;
import com.lemnt.order.application.promotion.executor.BargainRecordCreateCmdExe;
import com.lemnt.order.application.promotion.executor.BargainRecordPageQueryExe;
import com.lemnt.order.application.promotion.executor.PromotionOrderPlaceCmdExe;
import com.lemnt.order.application.promotion.qry.BargainHelpPageQry;
import com.lemnt.order.application.promotion.qry.BargainRecordPageQry;
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
@RequestMapping("/mall/app/bargain")
@RequiredArgsConstructor
public class AppBargainOrderController {
    private final PromotionOrderPlaceCmdExe orderPlace;
    @PostMapping("/orders")
    @Operation(summary = "砍价成功后下单")
    public R<AppPromotionOrderRes> order(@Valid @RequestBody AppPromotionOrderCreateReq req) {
        return R.ok(
                AppPromotionOrderConverter.res(
                        orderPlace.execute(
                                AppPromotionOrderConverter.cmd(
                                        SecurityUtils.getUserId(), "BARGAIN", req))));
    }
}
