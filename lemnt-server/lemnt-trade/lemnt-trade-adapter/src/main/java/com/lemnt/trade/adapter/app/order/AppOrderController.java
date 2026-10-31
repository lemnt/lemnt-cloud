package com.lemnt.trade.adapter.app.order;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.trade.adapter.app.order.converter.AppOrderResConverter;
import com.lemnt.trade.adapter.app.order.req.AppOrderPageReq;
import com.lemnt.trade.adapter.app.order.res.AppOrderDetailRes;
import com.lemnt.trade.adapter.app.order.res.AppOrderStatusCountRes;
import com.lemnt.trade.adapter.app.order.res.AppOrderSummaryRes;
import com.lemnt.trade.adapter.app.order.res.AppOrderTrackRes;
import com.lemnt.trade.application.fulfillment.executor.MemberOrderDetailQueryExe;
import com.lemnt.trade.application.fulfillment.executor.MemberOrderPageQueryExe;
import com.lemnt.trade.application.fulfillment.executor.MemberOrderStatusCountQueryExe;
import com.lemnt.trade.application.fulfillment.executor.MemberOrderTrackQueryExe;
import com.lemnt.trade.application.fulfillment.executor.OrderReceiveCmdExe;
import com.lemnt.trade.application.fulfillment.qry.MemberOrderPageQry;
import com.lemnt.trade.application.order.executor.TradeOrderCloseCmdExe;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

/** 会员本人订单中心，身份仅取网关已认证上下文。 */
@Tag(name = "会员订单中心")
@RestController
@RequiredArgsConstructor
@RequestMapping("/trade/app/orders")
public class AppOrderController {
    private final TradeOrderCloseCmdExe close;
    private final OrderReceiveCmdExe receive;
    private final MemberOrderPageQueryExe pageQuery;
    private final MemberOrderDetailQueryExe detailQuery;
    private final MemberOrderTrackQueryExe trackQuery;
    private final MemberOrderStatusCountQueryExe statusCountQuery;

    /** 本人订单分页，按状态或评价状态筛选。 */
    @GetMapping
    @Operation(summary = "本人订单分页")
    public R<AppOrderSummaryRes> page(@Valid @ModelAttribute AppOrderPageReq req) {
        var page = pageQuery.execute(SecurityUtils.getUserId(), new MemberOrderPageQry(req.status(),
                req.commentStatus(), req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 10 : req.pageSize()));
        return R.page(page.getRecords().stream().map(AppOrderResConverter::summary).toList(), page.getTotal());
    }

    /** 本人订单各状态数量，区分待评价与已完成。 */
    @GetMapping("/status-counts")
    @Operation(summary = "本人订单状态数量")
    public R<AppOrderStatusCountRes> statusCounts() {
        return R.ok(AppOrderResConverter.statusCount(statusCountQuery.execute(SecurityUtils.getUserId())));
    }

    /** 本人订单详情，他人订单统一返回不存在。 */
    @GetMapping("/{orderNo}")
    @Operation(summary = "本人订单详情")
    public R<AppOrderDetailRes> detail(@PathVariable String orderNo) {
        var value = detailQuery.execute(SecurityUtils.getUserId(), orderNo);
        return value == null ? R.fail("订单不存在") : R.ok(AppOrderResConverter.detail(value));
    }

    /** 本人订单物流轨迹，仅按本地登记运单查询。 */
    @GetMapping("/{orderNo}/tracks")
    @Operation(summary = "本人订单物流轨迹")
    public R<List<AppOrderTrackRes>> tracks(@PathVariable String orderNo) {
        var value = trackQuery.execute(SecurityUtils.getUserId(), orderNo);
        return value == null
                ? R.fail("订单不存在")
                : R.ok(value.stream().map(AppOrderResConverter::track).toList());
    }

    /** 取消本人未付款订单，库存与券统一异步回滚。 */
    @PostMapping("/{orderNo}/cancel")
    @Operation(summary = "取消本人未支付订单")
    public R<Boolean> cancel(@PathVariable String orderNo) {
        if (!close.executeForMember(SecurityUtils.getUserId(), orderNo)) {
            throw new ServiceException("订单不允许取消", 400);
        }
        return R.ok(true);
    }

    /** 核验订单归属后确认本人收货。 */
    @PostMapping("/{orderNo}/receive")
    @Operation(summary = "本人确认收货")
    public R<Boolean> receive(@PathVariable String orderNo) {
        receive.executeForMember(SecurityUtils.getUserId(), orderNo);
        return R.ok(true);
    }
}
