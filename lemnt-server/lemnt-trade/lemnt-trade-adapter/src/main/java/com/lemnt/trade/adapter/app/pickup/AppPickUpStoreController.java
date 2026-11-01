package com.lemnt.trade.adapter.app.pickup;

import com.lemnt.common.core.domain.R;
import com.lemnt.trade.adapter.app.pickup.converter.AppPickUpConverter;
import com.lemnt.trade.adapter.app.pickup.res.AppPickUpStoreRes;
import com.lemnt.trade.application.pickup.executor.AppPickUpStoreListQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序自提门店。 */
@Tag(name = "小程序自提门店")
@RestController
@RequestMapping("/trade/app/delivery/pick-up-stores")
@RequiredArgsConstructor
public class AppPickUpStoreController {
    private final AppPickUpStoreListQueryExe stores;

    @GetMapping
    @Operation(summary = "开启中的自提门店")
    public R<List<AppPickUpStoreRes>> list() {
        return R.ok(stores.execute().stream().map(AppPickUpConverter::store).toList());
    }
}
