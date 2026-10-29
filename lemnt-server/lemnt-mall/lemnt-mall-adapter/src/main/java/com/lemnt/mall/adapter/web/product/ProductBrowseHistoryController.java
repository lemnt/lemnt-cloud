package com.lemnt.mall.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.product.req.ProductBrowseHistoryPageReq;
import com.lemnt.mall.adapter.web.product.res.ProductBrowseHistoryPageRes;
import com.lemnt.mall.adapter.web.product.converter.ProductBrowseHistoryResConverter;
import com.lemnt.mall.application.product.executor.ProductBrowseHistoryPageQueryExe;
import com.lemnt.mall.adapter.web.product.converter.ProductBrowseHistoryReqConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 浏览足迹只读管理，按权限及显式筛选查询。 */
@RestController
@RequestMapping("/mall/product-browse-history")
@RequiredArgsConstructor
public class ProductBrowseHistoryController {
    private final ProductBrowseHistoryPageQueryExe query;

    /** 商品详情关联记录分页。 */
    @GetMapping
    @Operation(summary = "商品浏览足迹记录分页")
    @RequiresPermissions("mall:product-history:list")
    public R<ProductBrowseHistoryPageRes> page(@Valid @ModelAttribute ProductBrowseHistoryPageReq req) {
        var result = query.execute(ProductBrowseHistoryReqConverter.page(req));
        return R.page(result.getRecords().stream().map(ProductBrowseHistoryResConverter::page).toList(), result.getTotal());
    }
}
