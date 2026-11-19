package com.lemnt.promotion.adapter.web.article;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.promotion.adapter.web.article.converter.ArticleConverter;
import com.lemnt.promotion.adapter.web.article.req.ArticlePageReq;
import com.lemnt.promotion.adapter.web.article.req.ArticleSaveReq;
import com.lemnt.promotion.adapter.web.article.res.ArticleRes;
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

/** 商城后台文章内容管理。 */
@RestController
@RequestMapping("/mall/promotion/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService service;

    @GetMapping
    @RequiresPermissions("mall:promotion:list")
    public R<ArticleRes> page(@Valid @ModelAttribute ArticlePageReq req) {
        var result = service.page(ArticleConverter.page(req), false);
        return R.page(
                result.getRecords().stream().map(ArticleConverter::article).toList(),
                result.getTotal());
    }

    @GetMapping("/{id}")
    @RequiresPermissions("mall:promotion:query")
    public R<ArticleRes> detail(@PathVariable Long id) {
        var result = service.detail(id);
        return result == null ? R.fail(404, "记录不存在") : R.ok(ArticleConverter.article(result));
    }

    @PostMapping
    @RequiresPermissions("mall:promotion:create")
    public R<Long> create(@Valid @RequestBody ArticleSaveReq req) {
        return R.ok(service.save(ArticleConverter.save(null, req)));
    }

    @PutMapping("/{id}")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody ArticleSaveReq req) {
        service.save(ArticleConverter.save(id, req));
        return R.ok(true);
    }

    @DeleteMapping("/{id}")
    @RequiresPermissions("mall:promotion:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(service.delete(id));
    }
}
