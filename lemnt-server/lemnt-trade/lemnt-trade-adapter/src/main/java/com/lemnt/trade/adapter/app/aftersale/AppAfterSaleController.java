package com.lemnt.trade.adapter.app.aftersale;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.trade.adapter.app.aftersale.converter.AppAfterSaleResConverter;
import com.lemnt.trade.adapter.app.aftersale.req.AppAfterSaleCreateReq;
import com.lemnt.trade.adapter.app.aftersale.req.AppAfterSalePageReq;
import com.lemnt.trade.adapter.app.aftersale.req.AppAfterSaleReturnReq;
import com.lemnt.trade.adapter.app.aftersale.res.AppAfterSaleItemRes;
import com.lemnt.trade.adapter.app.aftersale.res.AppAfterSaleRes;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleCreateCmd;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleReturnCmd;
import com.lemnt.trade.application.aftersale.executor.AfterSaleCancelCmdExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleCreateCmdExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleReturnCmdExe;
import com.lemnt.trade.application.aftersale.executor.MemberAfterSaleDetailQueryExe;
import com.lemnt.trade.application.aftersale.executor.MemberAfterSaleItemListQueryExe;
import com.lemnt.trade.application.aftersale.executor.MemberAfterSalePageQueryExe;
import com.lemnt.trade.application.aftersale.qry.MemberAfterSalePageQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import java.util.List;

/** 会员本人售后中心，身份仅取网关已认证上下文。 */
@Tag(name = "会员售后中心")
@RestController
@RequiredArgsConstructor
@RequestMapping("/trade/app")
public class AppAfterSaleController {
    private final AfterSaleCancelCmdExe cancel;
    private final AfterSaleCreateCmdExe create;
    private final AfterSaleReturnCmdExe returned;
    private final MemberAfterSalePageQueryExe pageQuery;
    private final MemberAfterSaleDetailQueryExe detailQuery;
    private final MemberAfterSaleItemListQueryExe itemQuery;

    /** 本人订单可售后商品与剩余额度。 */
    @GetMapping("/orders/{orderNo}/after-sale-items")
    @Operation(summary = "本人订单可售后额度")
    public R<List<AppAfterSaleItemRes>> items(@PathVariable String orderNo) {
        var value = itemQuery.execute(SecurityUtils.getUserId(), orderNo);
        return value == null
                ? R.fail("订单不存在")
                : R.ok(value.stream().map(AppAfterSaleResConverter::item).toList());
    }

    /** 本人申请商品售后，金额由服务端计算。 */
    @PostMapping("/orders/{orderNo}/after-sales")
    @Operation(summary = "本人申请商品售后")
    public R<AppAfterSaleRes> create(@PathVariable String orderNo, @Valid @RequestBody AppAfterSaleCreateReq req) {
        var cmd = new AfterSaleCreateCmd(orderNo, req.requestNo(), req.orderItemId(), req.quantity(), req.type(),
                req.reason());
        return R.ok(AppAfterSaleResConverter.afterSale(create.executeForMember(cmd, SecurityUtils.getUserId())));
    }

    /** 本人售后分页。 */
    @GetMapping("/after-sales")
    @Operation(summary = "本人售后分页")
    public R<AppAfterSaleRes> page(@Valid @ModelAttribute AppAfterSalePageReq req) {
        var page = pageQuery.execute(SecurityUtils.getUserId(), new MemberAfterSalePageQry(req.orderNo(),
                req.status(), req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 10 : req.pageSize()));
        return R.page(page.getRecords().stream().map(AppAfterSaleResConverter::afterSale).toList(), page.getTotal());
    }

    /** 本人售后详情，他人售后统一返回不存在。 */
    @GetMapping("/after-sales/{no}")
    @Operation(summary = "本人售后详情")
    public R<AppAfterSaleRes> detail(@PathVariable String no) {
        var value = detailQuery.execute(SecurityUtils.getUserId(), no);
        return value == null ? R.fail("售后单不存在") : R.ok(AppAfterSaleResConverter.afterSale(value));
    }

    /** 本人登记退货运单，状态由服务端判定。 */
    @PostMapping("/after-sales/{no}/return")
    @Operation(summary = "本人登记退货运单")
    public R<AppAfterSaleRes> returned(@PathVariable String no, @Valid @RequestBody AppAfterSaleReturnReq req) {
        var cmd = new AfterSaleReturnCmd(no, req.carrierCode(), req.trackingNo());
        return R.ok(AppAfterSaleResConverter.afterSale(returned.executeForMember(cmd, SecurityUtils.getUserId())));
    }

    /** 不接受memberId参数，状态和归属由服务端判定。 */
    @PostMapping("/after-sales/{no}/cancel")
    @Operation(summary = "本人取消售后")
    public R<Boolean> cancel(@PathVariable String no) {
        return R.ok(cancel.execute(SecurityUtils.getUserId(), no));
    }
}
