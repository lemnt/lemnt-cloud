package com.lemnt.order.adapter.app.pickup;

import com.lemnt.common.core.domain.R;
import com.lemnt.order.adapter.app.pickup.converter.AppPickUpConverter;
import com.lemnt.order.adapter.app.pickup.res.AppPickUpStoreRes;
import com.lemnt.order.application.pickup.executor.AppPickUpStoreListQueryExe;
import com.lemnt.order.application.pickup.qry.AppPickUpStoreListQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 小程序自提门店。
 */
@Tag(name = "小程序自提门店")
@RestController
@RequestMapping("/trade/app/delivery/pick-up-stores")
@RequiredArgsConstructor
public class AppPickUpStoreController {
    private final AppPickUpStoreListQueryExe stores;

    @GetMapping
    @Operation(summary = "开启中的自提门店，可按当前位置距离排序")
    public R<List<AppPickUpStoreRes>> list(
        @RequestParam(required = false) BigDecimal latitude,
        @RequestParam(required = false) BigDecimal longitude) {
        return R.ok(
            stores.execute(new AppPickUpStoreListQry(latitude, longitude)).stream()
                .map(AppPickUpConverter::store)
                .toList());
    }
}
