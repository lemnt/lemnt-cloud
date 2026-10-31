package com.lemnt.trade.adapter.web.shipment;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.shipment.converter.ShipmentReqConverter;
import com.lemnt.trade.adapter.web.shipment.converter.ShipmentResConverter;
import com.lemnt.trade.adapter.web.shipment.req.ShipmentPageReq;
import com.lemnt.trade.adapter.web.shipment.res.ShipmentDetailRes;
import com.lemnt.trade.adapter.web.shipment.res.ShipmentPageRes;
import com.lemnt.trade.adapter.web.shipment.res.ShipmentTrackRes;
import com.lemnt.trade.application.fulfillment.executor.ShipmentDetailQueryExe;
import com.lemnt.trade.application.fulfillment.executor.ShipmentPageQueryExe;
import com.lemnt.trade.application.fulfillment.executor.ShipmentTrackListQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/** 后台物流单查询接口。 */
@Tag(name = "物流单管理")
@RestController
@RequestMapping("/trade/admin/shipments")
@RequiredArgsConstructor
public class ShipmentQueryController {
    private final ShipmentPageQueryExe pageQuery;
    private final ShipmentDetailQueryExe detailQuery;
    private final ShipmentTrackListQueryExe trackQuery;

    /** 分页查询物流单。 */
    @GetMapping
    @Operation(summary = "物流单分页")
    @RequiresPermissions("trade:shipment:list")
    public R<ShipmentPageRes> page(@Valid @ModelAttribute ShipmentPageReq req) {
        var page = pageQuery.execute(ShipmentReqConverter.page(req));
        return R.page(page.getRecords().stream().map(ShipmentResConverter::page).toList(), page.getTotal());
    }

    /** 查询含收件信息的物流详情。 */
    @GetMapping("/{orderNo}")
    @Operation(summary = "物流单详情")
    @RequiresPermissions("trade:shipment:query")
    public R<ShipmentDetailRes> detail(@PathVariable String orderNo) {
        var result = detailQuery.execute(orderNo);
        return result == null ? R.fail("物流单不存在") : R.ok(ShipmentResConverter.detail(result));
    }

    /** 按本地登记运单查询真实渠道轨迹。 */
    @GetMapping("/{orderNo}/tracks")
    @Operation(summary = "物流轨迹查询")
    @RequiresPermissions("trade:shipment:track")
    public R<List<ShipmentTrackRes>> tracks(@PathVariable String orderNo) {
        return R.ok(trackQuery.execute(orderNo).stream().map(ShipmentResConverter::track).toList());
    }
}
