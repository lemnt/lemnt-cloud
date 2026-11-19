package com.lemnt.promotion.adapter.web.article;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.promotion.adapter.web.article.converter.ArticleConverter;
import com.lemnt.promotion.adapter.web.article.req.ArticleCategoryPageReq;
import com.lemnt.promotion.adapter.web.article.req.ArticleCategorySaveReq;
import com.lemnt.promotion.adapter.web.article.res.ArticleCategoryRes;
import com.lemnt.promotion.application.article.service.ArticleService;

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

/** 商城后台文章内容管理。 */
@RestController
@RequestMapping("/mall/promotion/article-categories")
@RequiredArgsConstructor
public class ArticleCategoryController {
    private final ArticleService service;

    @GetMapping
    @RequiresPermissions("mall:promotion:list")
    public R<ArticleCategoryRes> page(@Valid @ModelAttribute ArticleCategoryPageReq req) {
        var result = service.categoryPage(ArticleConverter.categoryPage(req));
        return R.page(
                result.getRecords().stream().map(ArticleConverter::category).toList(),
                result.getTotal());
    }

    @GetMapping("/{id}")
    @RequiresPermissions("mall:promotion:query")
    public R<ArticleCategoryRes> detail(@PathVariable Long id) {
        var result = service.categoryDetail(id);
        return result == null ? R.fail(404, "记录不存在") : R.ok(ArticleConverter.category(result));
    }

    @PostMapping
    @RequiresPermissions("mall:promotion:create")
    public R<Long> create(@Valid @RequestBody ArticleCategorySaveReq req) {
        return R.ok(service.saveCategory(ArticleConverter.saveCategory(null, req)));
    }

    @PutMapping("/{id}")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> update(
            @PathVariable Long id, @Valid @RequestBody ArticleCategorySaveReq req) {
        service.saveCategory(ArticleConverter.saveCategory(id, req));
        return R.ok(true);
    }

    @DeleteMapping("/{id}")
    @RequiresPermissions("mall:promotion:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(service.deleteCategory(id));
    }

    @GetMapping("/enabled")
    @RequiresPermissions("mall:promotion:query")
    public R<List<ArticleCategoryRes>> enabled() {
        return R.ok(service.enabledCategories().stream().map(ArticleConverter::category).toList());
    }
}
