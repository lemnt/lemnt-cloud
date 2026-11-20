package com.lemnt.product.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.product.adapter.web.product.converter.ProductFavoriteReqConverter;
import com.lemnt.product.adapter.web.product.converter.ProductFavoriteResConverter;
import com.lemnt.product.adapter.web.product.req.ProductFavoritePageReq;
import com.lemnt.product.adapter.web.product.res.ProductFavoritePageRes;
import com.lemnt.product.application.product.executor.ProductFavoritePageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/**
 * 商品收藏管理
 */
@Tag(name = " 商品收藏管理")
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class ProductFavoriteController {
    private final ProductFavoritePageQueryExe favoritePageQueryExe;

    /**
     * 商品详情关联记录分页。
     */
    @GetMapping
    @Operation(summary = "商品收藏记录分页")
    @RequiresPermissions("product:favorite:list")
    public R<ProductFavoritePageRes> page(@Valid @ModelAttribute ProductFavoritePageReq req) {
        var result = favoritePageQueryExe.execute(ProductFavoriteReqConverter.page(req));
        return R.page(
            result.getRecords().stream().map(ProductFavoriteResConverter::page).toList(),
            result.getTotal());
    }
}
