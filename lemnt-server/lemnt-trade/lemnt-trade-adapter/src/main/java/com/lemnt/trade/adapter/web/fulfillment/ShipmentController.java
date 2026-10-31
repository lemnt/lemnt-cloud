package com.lemnt.trade.adapter.web.fulfillment;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.fulfillment.converter.FulfillmentReqConverter;
import com.lemnt.trade.adapter.web.fulfillment.converter.FulfillmentResConverter;
import com.lemnt.trade.adapter.web.fulfillment.req.ShipmentCreateReq;
import com.lemnt.trade.adapter.web.fulfillment.res.TradeShipmentShipRes;
import com.lemnt.trade.application.fulfillment.executor.ShipmentCreateCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 订单发货与收货接口。 */
@Tag(name = "订单发货与收货")
@RestController
@RequestMapping("/trade/admin/orders")
@RequiredArgsConstructor
public class ShipmentController {
    private final ShipmentCreateCmdExe shipmentCreateCmdExe;

    @PostMapping("/{orderNo}/shipment")
    @Operation(summary = "已支付且库存确认完成后登记发货，幂等且物流不可改写")
    @RequiresPermissions("trade:fulfillment:ship")
    public R<TradeShipmentShipRes> ship(
            @PathVariable String orderNo, @Valid @RequestBody ShipmentCreateReq req) {
        return R.ok(
                FulfillmentResConverter.shipmentToTradeShipmentShipRes(
                shipmentCreateCmdExe.execute(FulfillmentReqConverter.shipment(orderNo, req))));
    }
}
