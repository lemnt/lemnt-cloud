package com.lemnt.mall.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.product.req.ProductFavoritePageReq;
import com.lemnt.mall.adapter.web.product.res.ProductFavoritePageRes;
import com.lemnt.mall.adapter.web.product.converter.ProductFavoriteResConverter;
import com.lemnt.mall.application.product.executor.ProductFavoritePageQueryExe;
import com.lemnt.mall.adapter.web.product.converter.ProductFavoriteReqConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 商品收藏只读管理，按权限及显式筛选查询。 */
@RestController
@RequestMapping("/mall/product-favorites")
@RequiredArgsConstructor
public class ProductFavoriteController {
    private final ProductFavoritePageQueryExe query;

    /** 商品详情关联记录分页。 */
    @GetMapping
    @Operation(summary = "商品收藏记录分页")
    @RequiresPermissions("mall:product-favorite:list")
    public R<ProductFavoritePageRes> page(@Valid @ModelAttribute ProductFavoritePageReq req) {
        var result = query.execute(ProductFavoriteReqConverter.page(req));
        return R.page(result.getRecords().stream().map(ProductFavoriteResConverter::page).toList(), result.getTotal());
    }
}
