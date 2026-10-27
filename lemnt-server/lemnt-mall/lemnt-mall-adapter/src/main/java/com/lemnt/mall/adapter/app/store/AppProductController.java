package com.lemnt.mall.adapter.app.store;

import com.lemnt.common.core.domain.R;
import com.lemnt.mall.adapter.app.store.converter.StoreReqConverter;
import com.lemnt.mall.adapter.app.store.converter.StoreResConverter;
import com.lemnt.mall.adapter.app.store.req.StorePageReq;
import com.lemnt.mall.adapter.app.store.res.StoreDetailRes;
import com.lemnt.mall.adapter.app.store.res.StoreProductRes;
import com.lemnt.mall.application.store.executor.StoreProductDetailQueryExe;
import com.lemnt.mall.application.store.executor.StoreProductPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 小程序商品浏览接口。 */
@Tag(name = "小程序商品浏览")
@RestController
@RequestMapping("/mall/app")
@RequiredArgsConstructor
public class AppProductController {
    private final StoreProductPageQueryExe storeProductPageQueryExe;
    private final StoreProductDetailQueryExe storeProductDetailQueryExe;

    @GetMapping("/products")
    @Operation(summary = "公开在售商品分页，服务端价格区间")
    public R<StoreProductRes> products(@Valid @ModelAttribute StorePageReq req) {
        var page = storeProductPageQueryExe.execute(StoreReqConverter.page(req));
        return R.page(page.records().stream().map(StoreResConverter::product).toList(), page.total());
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "公开在售商品详情；不存在及隐藏均为null")
    public R<StoreDetailRes> product(@PathVariable Long id) {
        return R.ok(StoreResConverter.detail(storeProductDetailQueryExe.execute(id)));
    }
}
