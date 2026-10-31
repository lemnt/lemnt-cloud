package com.lemnt.mall.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.product.converter.ProductManagementReqConverter;
import com.lemnt.mall.adapter.web.product.converter.ProductManagementResConverter;
import com.lemnt.mall.adapter.web.product.req.ProductSpuBatchReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpuCountReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpuSimpleReq;
import com.lemnt.mall.adapter.web.product.res.ProductSpuBatchRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpuCountRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpuSimpleRes;
import com.lemnt.mall.application.product.executor.ProductSpuBatchDetailQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpuCountQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpuDeleteCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpuRecycleCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpuRestoreCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpuSimpleListQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/** 商品回收与选择接口。 */
@Tag(name = "商品回收与选择")
@RestController
@RequestMapping("/mall/spus")
@RequiredArgsConstructor
public class ProductSpuManagementController {
    private final ProductSpuRecycleCmdExe productSpuRecycleCmdExe;
    private final ProductSpuRestoreCmdExe productSpuRestoreCmdExe;
    private final ProductSpuDeleteCmdExe productSpuDeleteCmdExe;
    private final ProductSpuCountQueryExe productSpuCountQueryExe;
    private final ProductSpuSimpleListQueryExe productSpuSimpleListQueryExe;
    private final ProductSpuBatchDetailQueryExe productSpuBatchDetailQueryExe;

    /** 下架商品放入回收站。 */
    @PostMapping("/{id}/recycle")
    @Operation(summary = "商品放入回收站")
    @RequiresPermissions("mall:spu:edit")
    public R<Boolean> recycle(@PathVariable Long id) {
        return R.ok(productSpuRecycleCmdExe.execute(id));
    }

    /** 恢复为下架，不能直接恢复为上架。 */
    @PostMapping("/{id}/restore")
    @Operation(summary = "恢复回收站商品")
    @RequiresPermissions("mall:spu:edit")
    public R<Boolean> restore(@PathVariable Long id) {
        return R.ok(productSpuRestoreCmdExe.execute(id));
    }

    /** 删除仍保留历史编号。 */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除回收站商品")
    @RequiresPermissions("mall:spu:remove")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(productSpuDeleteCmdExe.execute(id));
    }

    /** 查询同筛选条件下各状态数量。 */
    @GetMapping("/count")
    @Operation(summary = "商品状态统计")
    @RequiresPermissions("mall:spu:list")
    public R<ProductSpuCountRes> count(@Valid @ModelAttribute ProductSpuCountReq req) {
        return R.ok(ProductManagementResConverter.count(productSpuCountQueryExe.execute(ProductManagementReqConverter.counts(req))));
    }

    /** 有界上架商品选择。 */
    @GetMapping("/simple")
    @Operation(summary = "上架商品选择查询")
    @RequiresPermissions("mall:spu:query")
    public R<List<ProductSpuSimpleRes>> simple(@Valid @ModelAttribute ProductSpuSimpleReq req) {
        return R.ok(productSpuSimpleListQueryExe.execute(ProductManagementReqConverter.simple(req)).stream().map(ProductManagementResConverter::simple).toList());
    }

    /** 批量查询真实详情。 */
    @GetMapping("/batch")
    @Operation(summary = "批量商品详情")
    @RequiresPermissions("mall:spu:query")
    public R<List<ProductSpuBatchRes>> batch(@Valid @ModelAttribute ProductSpuBatchReq req) {
        return R.ok(productSpuBatchDetailQueryExe.execute(req.ids()).stream().map(ProductManagementResConverter::batch).toList());
    }
}
