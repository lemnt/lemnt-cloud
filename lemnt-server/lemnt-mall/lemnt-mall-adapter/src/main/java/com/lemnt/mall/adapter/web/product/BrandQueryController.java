package com.lemnt.mall.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.product.converter.ProductManagementReqConverter;
import com.lemnt.mall.adapter.web.product.converter.ProductManagementResConverter;
import com.lemnt.mall.adapter.web.product.req.BrandPageReq;
import com.lemnt.mall.adapter.web.product.req.BrandSimpleReq;
import com.lemnt.mall.adapter.web.product.res.BrandPageRes;
import com.lemnt.mall.adapter.web.product.res.BrandSimpleRes;
import com.lemnt.mall.application.product.executor.BrandPageQueryExe;
import com.lemnt.mall.application.product.executor.BrandSimpleListQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/** 品牌查询补充接口。 */
@Tag(name = "品牌查询补充")
@RestController
@RequestMapping("/mall/brands")
@RequiredArgsConstructor
public class BrandQueryController {
    private final BrandPageQueryExe brandPageQueryExe;
    private final BrandSimpleListQueryExe brandSimpleListQueryExe;

    /** 品牌管理分页。 */
    @GetMapping("/page")
    @Operation(summary = "品牌分页")
    @RequiresPermissions("mall:brand:list")
    public R<BrandPageRes> page(@Valid @ModelAttribute BrandPageReq req) {
        var value = brandPageQueryExe.execute(ProductManagementReqConverter.brands(req));
        return R.page(value.getRecords().stream().map(ProductManagementResConverter::brand).toList(), value.getTotal());
    }

    /** 查询启用品牌供选择。 */
    @GetMapping("/simple")
    @Operation(summary = "启用品牌选择查询")
    @RequiresPermissions("mall:brand:query")
    public R<List<BrandSimpleRes>> simple(@Valid @ModelAttribute BrandSimpleReq req) {
        return R.ok(brandSimpleListQueryExe.execute(ProductManagementReqConverter.brandSimple(req)).stream().map(ProductManagementResConverter::brandSimple).toList());
    }
}
