package com.lemnt.trade.adapter.web.aftersale;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.trade.adapter.web.aftersale.converter.AfterSalePaymentResConverter;
import com.lemnt.trade.adapter.web.aftersale.converter.AfterSaleReqConverter;
import com.lemnt.trade.adapter.web.aftersale.converter.AfterSaleResConverter;
import com.lemnt.trade.adapter.web.aftersale.req.AfterSaleCreateReq;
import com.lemnt.trade.adapter.web.aftersale.req.AfterSalePageReq;
import com.lemnt.trade.adapter.web.aftersale.req.AfterSaleRefuseReq;
import com.lemnt.trade.adapter.web.aftersale.req.AfterSaleRejectReq;
import com.lemnt.trade.adapter.web.aftersale.req.AfterSaleReturnReq;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleAgreeRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleCreateRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleDetailRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleItemRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSalePageRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleReceiveRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleRefundRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleRefuseRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleRejectRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleReturnRes;
import com.lemnt.trade.application.aftersale.executor.AfterSaleAgreeCmdExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleCreateCmdExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleDetailQueryExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleItemListQueryExe;
import com.lemnt.trade.application.aftersale.executor.AfterSalePageQueryExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleReceiveCmdExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleRefuseCmdExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleRejectCmdExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleReturnCmdExe;
import com.lemnt.trade.application.paymentrefund.executor.PaymentRefundCreateCmdExe;

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

/** 商品售后管理接口。 */
@Tag(name = "商品售后管理")
@RestController
@RequestMapping("/trade/admin")
@RequiredArgsConstructor
public class AfterSaleController {
    private final AfterSaleCreateCmdExe create;
    private final AfterSaleAgreeCmdExe agree;
    private final AfterSaleRejectCmdExe reject;
    private final AfterSaleReturnCmdExe returned;
    private final AfterSaleReceiveCmdExe receive;
    private final AfterSaleRefuseCmdExe refuse;
    private final AfterSalePageQueryExe page;
    private final AfterSaleDetailQueryExe detail;
    private final AfterSaleItemListQueryExe items;
    private final PaymentRefundCreateCmdExe refund;

    /** 同意仅退款或退货已收货后发起原支付退款，状态按真实渠道结果更新。 */
    @PostMapping("/after-sales/{no}/refund")
    @Operation(summary = "确认售后原路退款")
    @RequiresPermissions("trade:after-sale:refund")
    public R<AfterSaleRefundRes> refund(@PathVariable String no) {
        return R.ok(AfterSalePaymentResConverter.toRes(refund.executeForAdmin(no, SecurityUtils.getUserId())));
    }

    /** 查询可售后商品与额度。 */
    @GetMapping("/orders/{orderNo}/after-sale-items")
    @Operation(summary = "可售后商品额度")
    @RequiresPermissions("trade:after-sale:query")
    public R<List<AfterSaleItemRes>> items(@PathVariable String orderNo) {
        return R.ok(items.execute(orderNo).stream().map(AfterSaleResConverter::item).toList());
    }

    /** 创建商品部分或全部数量的售后。 */
    @PostMapping("/orders/{orderNo}/after-sales")
    @Operation(summary = "申请商品售后")
    @RequiresPermissions("trade:after-sale:create")
    public R<AfterSaleCreateRes> create(@PathVariable String orderNo, @Valid @RequestBody AfterSaleCreateReq req) {
        return R.ok(AfterSaleResConverter.create(create.executeForAdmin(AfterSaleReqConverter.create(orderNo, req), SecurityUtils.getUserId())));
    }

    /** 分页查询售后。 */
    @GetMapping("/after-sales")
    @Operation(summary = "商品售后分页")
    @RequiresPermissions("trade:after-sale:list")
    public R<AfterSalePageRes> page(@Valid @ModelAttribute AfterSalePageReq req) {
        var result = page.execute(AfterSaleReqConverter.page(req));
        return R.page(result.getRecords().stream().map(AfterSaleResConverter::page).toList(), result.getTotal());
    }

    /** 查询售后详情。 */
    @GetMapping("/after-sales/{no}")
    @Operation(summary = "商品售后详情")
    @RequiresPermissions("trade:after-sale:query")
    public R<AfterSaleDetailRes> detail(@PathVariable String no) {
        var value = detail.execute(no);
        return value == null ? R.fail("售后单不存在") : R.ok(AfterSaleResConverter.detail(value));
    }

    /** 同意售后后按方式分别进入等待退货或等待退款。 */
    @PostMapping("/after-sales/{no}/agree")
    @Operation(summary = "同意商品售后")
    @RequiresPermissions("trade:after-sale:agree")
    public R<AfterSaleAgreeRes> agree(@PathVariable String no) {
        return R.ok(AfterSaleResConverter.agree(agree.executeForAdmin(no, SecurityUtils.getUserId())));
    }

    /** 拒绝尚未审核的申请。 */
    @PostMapping("/after-sales/{no}/reject")
    @Operation(summary = "拒绝商品售后")
    @RequiresPermissions("trade:after-sale:reject")
    public R<AfterSaleRejectRes> reject(@PathVariable String no, @Valid @RequestBody AfterSaleRejectReq req) {
        return R.ok(AfterSaleResConverter.reject(reject.executeForAdmin(AfterSaleReqConverter.reject(no, req), SecurityUtils.getUserId())));
    }

    /** 登记买家寄回的物流信息。 */
    @PostMapping("/after-sales/{no}/return")
    @Operation(summary = "登记退货运单")
    @RequiresPermissions("trade:after-sale:return")
    public R<AfterSaleReturnRes> returned(@PathVariable String no, @Valid @RequestBody AfterSaleReturnReq req) {
        return R.ok(AfterSaleResConverter.returned(returned.executeForAdmin(AfterSaleReqConverter.returned(no, req), SecurityUtils.getUserId())));
    }

    /** 确认收到退货后允许退款。 */
    @PostMapping("/after-sales/{no}/receive")
    @Operation(summary = "确认退货收货")
    @RequiresPermissions("trade:after-sale:receive")
    public R<AfterSaleReceiveRes> receive(@PathVariable String no) {
        return R.ok(AfterSaleResConverter.receive(receive.executeForAdmin(no, SecurityUtils.getUserId())));
    }

    /** 拒收必须记录原因。 */
    @PostMapping("/after-sales/{no}/refuse")
    @Operation(summary = "拒绝退货收货")
    @RequiresPermissions("trade:after-sale:refuse")
    public R<AfterSaleRefuseRes> refuse(@PathVariable String no, @Valid @RequestBody AfterSaleRefuseReq req) {
        return R.ok(AfterSaleResConverter.refuse(refuse.executeForAdmin(AfterSaleReqConverter.refuse(no, req), SecurityUtils.getUserId())));
    }
}
