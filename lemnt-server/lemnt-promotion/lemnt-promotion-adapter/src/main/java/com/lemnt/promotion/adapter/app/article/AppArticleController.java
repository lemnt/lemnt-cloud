package com.lemnt.promotion.adapter.app.article;

import com.lemnt.common.core.domain.R;
import com.lemnt.promotion.adapter.web.article.converter.ArticleConverter;
import com.lemnt.promotion.adapter.web.article.req.ArticlePageReq;
import com.lemnt.promotion.adapter.web.article.res.ArticleCategoryRes;
import com.lemnt.promotion.adapter.web.article.res.ArticleRes;
import com.lemnt.promotion.application.article.service.ArticleService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/** 公开文章阅读与推荐，不透出停用内容。 */
@RestController
@RequestMapping("/mall/app")
@RequiredArgsConstructor
public class AppArticleController {
    private final ArticleService service;

    @GetMapping("/articles")
    public R<ArticleRes> page(@Valid @ModelAttribute ArticlePageReq req) {
        var result = service.page(ArticleConverter.page(req), true);
        return R.page(
                result.getRecords().stream().map(ArticleConverter::article).toList(),
                result.getTotal());
    }

    @GetMapping("/articles/detail")
    public R<ArticleRes> detail(
            @RequestParam(required = false) Long id, @RequestParam(required = false) String title) {
        var value = service.published(id, title);
        return value == null ? R.fail(404, "文章不可见") : R.ok(ArticleConverter.article(value));
    }

    @PutMapping("/articles/{id}/browse")
    public R<Boolean> browse(@PathVariable Long id) {
        return R.ok(service.browse(id));
    }

    @GetMapping("/article-categories")
    public R<List<ArticleCategoryRes>> categories() {
        return R.ok(service.enabledCategories().stream().map(ArticleConverter::category).toList());
    }
}
