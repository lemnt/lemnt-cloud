package com.lemnt.promotion.infrastructure.article.gateway;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.promotion.domain.article.gateway.ArticleGateway;
import com.lemnt.promotion.domain.article.model.Article;
import com.lemnt.promotion.domain.article.model.ArticleCategory;
import com.lemnt.promotion.infrastructure.article.dataobject.ArticleCategoryDO;
import com.lemnt.promotion.infrastructure.article.dataobject.ArticleDO;
import com.lemnt.promotion.infrastructure.article.mapper.ArticleCategoryMapper;
import com.lemnt.promotion.infrastructure.article.mapper.ArticleMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ArticleGatewayImpl implements ArticleGateway {
    private final ArticleMapper articles;
    private final ArticleCategoryMapper categories;

    @Override
    public Article find(Long id) {
        return article(articles.selectById(id));
    }

    @Override
    public Article published(Long id, String title) {
        return articles
                .selectPage(
                        new Page<ArticleDO>(1, 1, false),
                        Wrappers.<ArticleDO>lambdaQuery()
                                .eq(ArticleDO::getStatus, 0)
                                .eq(id != null, ArticleDO::getId, id)
                                .eq(id == null, ArticleDO::getTitle, title)
                                .orderByDesc(ArticleDO::getId))
                .getRecords()
                .stream()
                .findFirst()
                .map(ArticleGatewayImpl::article)
                .orElse(null);
    }

    @Override
    public PageResult<Article> articles(
            String title,
            Long categoryId,
            Integer status,
            Boolean hot,
            Boolean banner,
            boolean published,
            int pageNum,
            int pageSize) {
        var rows =
                articles.selectPage(
                        new Page<ArticleDO>(pageNum, pageSize),
                        Wrappers.<ArticleDO>lambdaQuery()
                                .like(title != null && !title.isBlank(), ArticleDO::getTitle, title)
                                .eq(categoryId != null, ArticleDO::getCategoryId, categoryId)
                                .eq(published, ArticleDO::getStatus, 0)
                                .eq(!published && status != null, ArticleDO::getStatus, status)
                                .eq(hot != null, ArticleDO::getRecommendHot, hot)
                                .eq(banner != null, ArticleDO::getRecommendBanner, banner)
                                .orderByDesc(ArticleDO::getSort)
                                .orderByDesc(ArticleDO::getId));
        return PageResult.of(
                rows.getRecords().stream().map(ArticleGatewayImpl::article).toList(),
                rows.getTotal());
    }

    @Override
    public Long save(Article v) {
        try {
            if (v.id() == null) {
                var row = new ArticleDO();
                row.setCategoryId(v.categoryId());
                row.setSpuId(v.spuId());
                row.setTitle(v.title());
                row.setAuthor(v.author());
                row.setPicUrl(v.picUrl());
                row.setIntroduction(v.introduction());
                row.setSort(v.sort());
                row.setStatus(v.status());
                row.setRecommendHot(v.recommendHot());
                row.setRecommendBanner(v.recommendBanner());
                row.setContent(v.content());
                articles.insert(row);
                return row.getId();
            }
            if (articles.update(
                            null,
                            Wrappers.<ArticleDO>lambdaUpdate()
                                    .eq(ArticleDO::getId, v.id())
                                    .set(ArticleDO::getCategoryId, v.categoryId())
                                    .set(ArticleDO::getSpuId, v.spuId())
                                    .set(ArticleDO::getTitle, v.title())
                                    .set(ArticleDO::getAuthor, v.author())
                                    .set(ArticleDO::getPicUrl, v.picUrl())
                                    .set(ArticleDO::getIntroduction, v.introduction())
                                    .set(ArticleDO::getSort, v.sort())
                                    .set(ArticleDO::getStatus, v.status())
                                    .set(ArticleDO::getRecommendHot, v.recommendHot())
                                    .set(ArticleDO::getRecommendBanner, v.recommendBanner())
                                    .set(ArticleDO::getContent, v.content()))
                    != 1) throw new ServiceException("文章不存在", 404);
            return v.id();
        } catch (DataIntegrityViolationException error) {
            throw new ServiceException("文章分类已变化，请刷新后重试", 409);
        }
    }

    @Override
    public boolean deleteArticle(Long id) {
        return articles.deleteById(id) == 1;
    }

    @Override
    public boolean addBrowse(Long id) {
        return articles.update(
                        null,
                        Wrappers.<ArticleDO>lambdaUpdate()
                                .eq(ArticleDO::getId, id)
                                .eq(ArticleDO::getStatus, 0)
                                .setIncrBy(ArticleDO::getBrowseCount, 1))
                == 1;
    }

    @Override
    public ArticleCategory category(Long id) {
        return category(categories.selectById(id));
    }

    @Override
    public PageResult<ArticleCategory> categories(
            String name, Integer status, int pageNum, int pageSize) {
        var rows =
                categories.selectPage(
                        new Page<ArticleCategoryDO>(pageNum, pageSize),
                        Wrappers.<ArticleCategoryDO>lambdaQuery()
                                .like(
                                        name != null && !name.isBlank(),
                                        ArticleCategoryDO::getName,
                                        name)
                                .eq(status != null, ArticleCategoryDO::getStatus, status)
                                .orderByDesc(ArticleCategoryDO::getSort)
                                .orderByDesc(ArticleCategoryDO::getId));
        return PageResult.of(
                rows.getRecords().stream().map(ArticleGatewayImpl::category).toList(),
                rows.getTotal());
    }

    @Override
    public List<ArticleCategory> enabledCategories() {
        return categories
                .selectList(
                        Wrappers.<ArticleCategoryDO>lambdaQuery()
                                .eq(ArticleCategoryDO::getStatus, 0)
                                .orderByDesc(ArticleCategoryDO::getSort)
                                .orderByDesc(ArticleCategoryDO::getId))
                .stream()
                .map(ArticleGatewayImpl::category)
                .toList();
    }

    @Override
    public Long saveCategory(ArticleCategory v) {
        if (v.id() == null) {
            var row = new ArticleCategoryDO();
            row.setName(v.name());
            row.setPicUrl(v.picUrl());
            row.setSort(v.sort());
            row.setStatus(v.status());
            categories.insert(row);
            return row.getId();
        }
        if (categories.update(
                        null,
                        Wrappers.<ArticleCategoryDO>lambdaUpdate()
                                .eq(ArticleCategoryDO::getId, v.id())
                                .set(ArticleCategoryDO::getName, v.name())
                                .set(ArticleCategoryDO::getPicUrl, v.picUrl())
                                .set(ArticleCategoryDO::getSort, v.sort())
                                .set(ArticleCategoryDO::getStatus, v.status()))
                != 1) throw new ServiceException("文章分类不存在", 404);
        return v.id();
    }

    @Override
    public boolean deleteCategory(Long id) {
        try {
            return categories.deleteById(id) == 1;
        } catch (DataIntegrityViolationException error) {
            throw new ServiceException("分类仍被文章引用", 409);
        }
    }

    private static Article article(ArticleDO row) {
        return row == null
                ? null
                : new Article(
                        row.getId(),
                        row.getCategoryId(),
                        row.getSpuId(),
                        row.getTitle(),
                        row.getAuthor(),
                        row.getPicUrl(),
                        row.getIntroduction(),
                        row.getSort(),
                        row.getStatus(),
                        row.getRecommendHot(),
                        row.getRecommendBanner(),
                        row.getContent(),
                        row.getBrowseCount());
    }

    private static ArticleCategory category(ArticleCategoryDO row) {
        return row == null
                ? null
                : new ArticleCategory(
                        row.getId(),
                        row.getName(),
                        row.getPicUrl(),
                        row.getSort(),
                        row.getStatus());
    }
}
