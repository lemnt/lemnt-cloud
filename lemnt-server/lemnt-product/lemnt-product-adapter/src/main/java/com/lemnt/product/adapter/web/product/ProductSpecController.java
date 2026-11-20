package com.lemnt.product.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.product.adapter.web.product.converter.ProductSpecReqConverter;
import com.lemnt.product.adapter.web.product.converter.ProductSpecResConverter;
import com.lemnt.product.adapter.web.product.req.ProductSpecCreateReq;
import com.lemnt.product.adapter.web.product.req.ProductSpecPageReq;
import com.lemnt.product.adapter.web.product.req.ProductSpecSimpleReq;
import com.lemnt.product.adapter.web.product.req.ProductSpecUpdateReq;
import com.lemnt.product.adapter.web.product.res.ProductSpecCreateRes;
import com.lemnt.product.adapter.web.product.res.ProductSpecDetailRes;
import com.lemnt.product.adapter.web.product.res.ProductSpecPageRes;
import com.lemnt.product.adapter.web.product.res.ProductSpecSimpleRes;
import com.lemnt.product.adapter.web.product.res.ProductSpecUpdateRes;
import com.lemnt.product.application.product.executor.ProductSpecCreateCmdExe;
import com.lemnt.product.application.product.executor.ProductSpecDeleteCmdExe;
import com.lemnt.product.application.product.executor.ProductSpecDetailQueryExe;
import com.lemnt.product.application.product.executor.ProductSpecPageQueryExe;
import com.lemnt.product.application.product.executor.ProductSpecSimpleListQueryExe;
import com.lemnt.product.application.product.executor.ProductSpecUpdateCmdExe;

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

/**
 * 规格字典管理接口。
 */
@Tag(name = "商品规格项管理")
@RestController
@RequestMapping("/specs")
@RequiredArgsConstructor
public class ProductSpecController {
    private final ProductSpecPageQueryExe specPageQueryExe;
    private final ProductSpecSimpleListQueryExe specSimpleListQueryExe;
    private final ProductSpecDetailQueryExe specDetailQueryExe;
    private final ProductSpecCreateCmdExe specCreateCmdExe;
    private final ProductSpecUpdateCmdExe specUpdateCmdExe;
    private final ProductSpecDeleteCmdExe specDeleteCmdExe;

    /**
     * 分页查询。
     */
    @GetMapping
    @Operation(summary = "规格字典分页")
    @RequiresPermissions("product:spec:list")
    public R<ProductSpecPageRes> page(@Valid @ModelAttribute ProductSpecPageReq req) {
        var value = specPageQueryExe.execute(ProductSpecReqConverter.page(req));
        return R.page(
            value.getRecords().stream().map(ProductSpecResConverter::page).toList(),
            value.getTotal());
    }

    /**
     * 查询有限条选择数据。
     */
    @GetMapping("/simple")
    @Operation(summary = "规格字典选择查询")
    @RequiresPermissions("product:spec:query")
    public R<List<ProductSpecSimpleRes>> simple(@Valid @ModelAttribute ProductSpecSimpleReq req) {
        return R.ok(
            specSimpleListQueryExe.execute(ProductSpecReqConverter.simple(req)).stream()
                .map(ProductSpecResConverter::simple)
                .toList());
    }

    /**
     * 查询详情。
     */
    @GetMapping("/{id}")
    @Operation(summary = "规格字典详情")
    @RequiresPermissions("product:spec:query")
    public R<ProductSpecDetailRes> detail(@PathVariable Long id) {
        var value = specDetailQueryExe.execute(id);
        return value == null ? R.fail("字典不存在") : R.ok(ProductSpecResConverter.detail(value));
    }

    /**
     * 新增字典。
     */
    @PostMapping
    @Operation(summary = "新增规格字典")
    @RequiresPermissions("product:spec:add")
    public R<ProductSpecCreateRes> create(@Valid @RequestBody ProductSpecCreateReq req) {
        return R.ok(
            ProductSpecResConverter.create(
                specCreateCmdExe.execute(ProductSpecReqConverter.create(req))));
    }

    /**
     * 修改字典。
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改规格字典")
    @RequiresPermissions("product:spec:edit")
    public R<ProductSpecUpdateRes> update(
        @PathVariable Long id, @Valid @RequestBody ProductSpecUpdateReq req) {
        return R.ok(
            ProductSpecResConverter.update(
                specUpdateCmdExe.execute(ProductSpecReqConverter.update(id, req))));
    }

    /**
     * 删除无子值的规格项。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除无子值的规格项")
    @RequiresPermissions("product:spec:remove")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(specDeleteCmdExe.execute(id));
    }
}
