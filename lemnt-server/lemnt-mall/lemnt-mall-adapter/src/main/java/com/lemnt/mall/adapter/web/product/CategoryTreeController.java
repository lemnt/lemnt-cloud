package com.lemnt.mall.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.product.converter.ProductManagementResConverter;
import com.lemnt.mall.adapter.web.product.res.CategoryTreeRes;
import com.lemnt.mall.application.product.executor.CategoryTreeQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 分类树查询接口。 */
@Tag(name = "分类树查询")
@RestController
@RequestMapping("/mall/categories")
@RequiredArgsConstructor
public class CategoryTreeController {
    private final CategoryTreeQueryExe categoryTreeQueryExe;

    /** 返回真实父子分类树。 */
    @GetMapping("/tree")
    @Operation(summary = "分类树")
    @RequiresPermissions("mall:category:list")
    public R<List<CategoryTreeRes>> tree() {
        return R.ok(categoryTreeQueryExe.execute().stream().map(ProductManagementResConverter::tree).toList());
    }
}
