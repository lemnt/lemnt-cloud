package com.lemnt.mall.adapter.web.store;

import com.lemnt.common.core.domain.R;
import com.lemnt.mall.application.store.executor.StoreCatalogQueryExe;
import com.lemnt.mall.adapter.web.store.req.StorePageReq;
import com.lemnt.mall.adapter.web.store.res.*;
import com.lemnt.mall.adapter.web.store.converter.*;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.tags.Tag(name = "小程序公开商品目录")
@RestController
@RequestMapping("/mall/app")
@lombok.RequiredArgsConstructor
public class AppStoreController {
    private final StoreCatalogQueryExe queries;

    @GetMapping("/categories")
    @io.swagger.v3.oas.annotations.Operation(summary = "公开启用分类")
    public R<java.util.List<StoreCategoryRes>> categories() {
        return R.ok(queries.execute().stream().map(StoreResConverter::category).toList());
    }

    @GetMapping("/products")
    @io.swagger.v3.oas.annotations.Operation(summary = "公开在售商品分页，服务端价格区间")
    public R<StorePageRes> products(@jakarta.validation.Valid @ModelAttribute StorePageReq req) {
        return R.ok(StoreResConverter.page(queries.execute(StoreReqConverter.page(req))));
    }

    @GetMapping("/products/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "公开在售商品详情；不存在及隐藏均为null")
    public R<StoreDetailRes> product(@PathVariable Long id) {
        return R.ok(StoreResConverter.detail(queries.execute(id)));
    }
}
