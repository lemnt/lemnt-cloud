package com.lemnt.mall.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.product.converter.ProductSpecValueReqConverter;
import com.lemnt.mall.adapter.web.product.converter.ProductSpecValueResConverter;
import com.lemnt.mall.adapter.web.product.req.ProductSpecValueCreateReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpecValuePageReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpecValueSimpleReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpecValueUpdateReq;
import com.lemnt.mall.adapter.web.product.res.ProductSpecValueCreateRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpecValueDetailRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpecValuePageRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpecValueSimpleRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpecValueUpdateRes;
import com.lemnt.mall.application.product.executor.ProductSpecValueCreateCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpecValueDeleteCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpecValueDetailQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpecValuePageQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpecValueSimpleListQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpecValueUpdateCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/** 规格字典管理接口。 */
@Tag(name = "规格值管理")
@RestController
@RequestMapping("/mall/spec-values")
@RequiredArgsConstructor
public class ProductSpecValueController {
    private final ProductSpecValueCreateCmdExe productSpecValueCreateCmdExe;
    private final ProductSpecValueUpdateCmdExe productSpecValueUpdateCmdExe;
    private final ProductSpecValueDeleteCmdExe productSpecValueDeleteCmdExe;
    private final ProductSpecValueDetailQueryExe productSpecValueDetailQueryExe;
    private final ProductSpecValuePageQueryExe productSpecValuePageQueryExe;
    private final ProductSpecValueSimpleListQueryExe productSpecValueSimpleListQueryExe;

    /** 新增字典。 */
    @PostMapping
    @Operation(summary = "新增规格字典")
    @RequiresPermissions("mall:spec-value:add")
    public R<ProductSpecValueCreateRes> create(@Valid @RequestBody ProductSpecValueCreateReq req) {
        return R.ok(ProductSpecValueResConverter.create(productSpecValueCreateCmdExe.execute(ProductSpecValueReqConverter.create(req))));
    }

    /** 修改字典。 */
    @PutMapping("/{id}")
    @Operation(summary = "修改规格字典")
    @RequiresPermissions("mall:spec-value:edit")
    public R<ProductSpecValueUpdateRes> update(@PathVariable Long id, @Valid @RequestBody ProductSpecValueUpdateReq req) {
        return R.ok(ProductSpecValueResConverter.update(productSpecValueUpdateCmdExe.execute(ProductSpecValueReqConverter.update(id, req))));
    }

    /** 删除规格值并保留SKU快照。 */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除规格值并保留SKU快照")
    @RequiresPermissions("mall:spec-value:remove")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(productSpecValueDeleteCmdExe.execute(id));
    }

    /** 查询详情。 */
    @GetMapping("/{id}")
    @Operation(summary = "规格字典详情")
    @RequiresPermissions("mall:spec-value:query")
    public R<ProductSpecValueDetailRes> detail(@PathVariable Long id) {
        var value = productSpecValueDetailQueryExe.execute(id);
        return value == null ? R.fail("字典不存在") : R.ok(ProductSpecValueResConverter.detail(value));
    }

    /** 分页查询。 */
    @GetMapping
    @Operation(summary = "规格字典分页")
    @RequiresPermissions("mall:spec-value:list")
    public R<ProductSpecValuePageRes> page(@Valid @ModelAttribute ProductSpecValuePageReq req) {
        var value = productSpecValuePageQueryExe.execute(ProductSpecValueReqConverter.page(req));
        return R.page(value.getRecords().stream().map(ProductSpecValueResConverter::page).toList(), value.getTotal());
    }

    /** 查询有限条选择数据。 */
    @GetMapping("/simple")
    @Operation(summary = "规格字典选择查询")
    @RequiresPermissions("mall:spec-value:query")
    public R<List<ProductSpecValueSimpleRes>> simple(@Valid @ModelAttribute ProductSpecValueSimpleReq req) {
        return R.ok(productSpecValueSimpleListQueryExe.execute(ProductSpecValueReqConverter.simple(req)).stream().map(ProductSpecValueResConverter::simple).toList());
    }
}
