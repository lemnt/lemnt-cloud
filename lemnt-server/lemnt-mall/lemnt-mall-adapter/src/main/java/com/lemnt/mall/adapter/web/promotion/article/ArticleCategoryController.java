package com.lemnt.mall.adapter.web.promotion.article;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.promotion.article.req.*;
import com.lemnt.mall.adapter.web.promotion.article.res.*;
import com.lemnt.mall.adapter.web.promotion.article.converter.ArticleCategoryReqConverter;
import com.lemnt.mall.adapter.web.promotion.article.converter.ArticleCategoryResConverter;
import com.lemnt.mall.application.promotion.article.executor.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 文章分类管理接口。 */
@RestController
@RequestMapping("/mall/promotion/article-categories")
@RequiredArgsConstructor
public class ArticleCategoryController {
    private final ArticleCategoryCreateCmdExe create;
    private final ArticleCategoryUpdateCmdExe update;
    private final ArticleCategoryDeleteCmdExe delete;
    private final ArticleCategoryDetailQueryExe detail;
    private final ArticleCategoryPageQueryExe page;
    private final ArticleCategoryEnabledListQueryExe enabled;

    /** 分页查询。 */
    @GetMapping
    @Operation(summary = "文章分类分页")
    @RequiresPermissions("mall:article-category:list")
    public R<ArticleCategoryPageRes> page(@Valid @ModelAttribute ArticleCategoryPageReq req) {
        var result = page.execute(ArticleCategoryReqConverter.page(req));
        return R.page(result.getRecords().stream().map(ArticleCategoryResConverter::page).toList(), result.getTotal());
    }

    /** 查询详情。 */
    @GetMapping("/{id:[1-9][0-9]*}")
    @Operation(summary = "文章分类详情")
    @RequiresPermissions("mall:article-category:query")
    public R<ArticleCategoryDetailRes> detail(@PathVariable Long id) {
        return R.ok(ArticleCategoryResConverter.detail(detail.execute(id)));
    }

    /** 新增记录。 */
    @PostMapping
    @Operation(summary = "新增文章分类")
    @RequiresPermissions("mall:article-category:create")
    public R<ArticleCategoryCreateRes> create(@Valid @RequestBody ArticleCategoryCreateReq req) {
        return R.ok(new ArticleCategoryCreateRes(create.execute(ArticleCategoryReqConverter.create(req))));
    }

    /** 修改资料，不接受浏览次数。 */
    @PutMapping("/{id:[1-9][0-9]*}")
    @Operation(summary = "修改文章分类")
    @RequiresPermissions("mall:article-category:update")
    public R<ArticleCategoryUpdateRes> update(@PathVariable Long id, @Valid @RequestBody ArticleCategoryUpdateReq req) {
        return R.ok(new ArticleCategoryUpdateRes(update.execute(ArticleCategoryReqConverter.update(id, req))));
    }

    /** 删除记录。 */
    @DeleteMapping("/{id:[1-9][0-9]*}")
    @Operation(summary = "删除文章分类")
    @RequiresPermissions("mall:article-category:delete")
    public R<ArticleCategoryDeleteRes> delete(@PathVariable Long id) {
        return R.ok(new ArticleCategoryDeleteRes(delete.execute(id)));
    }

    /** 后台表单的启用分类选项。 */
    @GetMapping("/enabled")
    @Operation(summary = "启用文章分类选项")
    @RequiresPermissions("mall:article-category:list")
    public R<java.util.List<ArticleCategoryEnabledListRes>> enabled() {
        return R.ok(enabled.execute().stream().map(ArticleCategoryResConverter::enabled).toList());
    }

}
