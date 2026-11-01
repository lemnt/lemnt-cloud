package com.lemnt.trade.adapter.app.fulfillment;

import com.lemnt.common.core.domain.R;
import com.lemnt.trade.adapter.app.fulfillment.res.AppDeliveryExpressRes;
import com.lemnt.trade.application.fulfillment.executor.AppDeliveryExpressListQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序快递公司选择。 */
@Tag(name = "小程序快递公司")
@RestController
@RequestMapping("/trade/app/delivery")
@RequiredArgsConstructor
public class AppDeliveryExpressController {
    private final AppDeliveryExpressListQueryExe expresses;

    @GetMapping("/expresses")
    @Operation(summary = "启用的快递公司列表")
    public R<List<AppDeliveryExpressRes>> expresses() {
        return R.ok(expresses.execute().stream()
                .map(value -> new AppDeliveryExpressRes(value.id(), value.code(), value.name())).toList());
    }
}
