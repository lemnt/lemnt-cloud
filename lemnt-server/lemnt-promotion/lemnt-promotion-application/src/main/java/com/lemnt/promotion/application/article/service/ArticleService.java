package com.lemnt.promotion.application.article.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.promotion.application.article.cmd.ArticleCategorySaveCmd;
import com.lemnt.promotion.application.article.cmd.ArticleSaveCmd;
import com.lemnt.promotion.application.article.dto.ArticleCategoryDTO;
import com.lemnt.promotion.application.article.dto.ArticleDTO;
import com.lemnt.promotion.application.article.qry.ArticleCategoryPageQry;
import com.lemnt.promotion.application.article.qry.ArticlePageQry;
import com.lemnt.promotion.domain.article.gateway.ArticleGateway;
import com.lemnt.promotion.domain.article.model.Article;
import com.lemnt.promotion.domain.article.model.ArticleCategory;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleGateway gateway;

    /** 公开只读启用文章，标题查询取最新同名记录。 */
    public ArticleDTO published(Long id, String title) {
        if (id == null && (title == null || title.isBlank())) {
            throw new ServiceException("请提供文章编号或标题", 400);
        }
        return article(gateway.published(id, title));
    }

    public ArticleDTO detail(Long id) {
        return article(gateway.find(id));
    }

    public PageResult<ArticleDTO> page(ArticlePageQry q, boolean published) {
        var result =
                gateway.articles(
                        q.title(),
                        q.categoryId(),
                        q.status(),
                        q.recommendHot(),
                        q.recommendBanner(),
                        published,
                        q.pageNum(),
                        q.pageSize());
        return PageResult.of(
                result.getRecords().stream().map(ArticleService::article).toList(),
                result.getTotal());
    }

    /** 浏览量仅统计公开可读文章，由数据库原子递增。 */
    public boolean browse(Long id) {
        return gateway.addBrowse(id);
    }

    /** 外键同步保护分类删除与写入并发。 */
    public Long save(ArticleSaveCmd cmd) {
        if (gateway.category(cmd.categoryId()) == null) {
            throw new ServiceException("文章分类不存在", 400);
        }
        return gateway.save(
                new Article(
                        cmd.id(),
                        cmd.categoryId(),
                        cmd.spuId(),
                        cmd.title(),
                        cmd.author(),
                        cmd.picUrl(),
                        cmd.introduction(),
                        cmd.sort(),
                        cmd.status(),
                        cmd.recommendHot(),
                        cmd.recommendBanner(),
                        cmd.content(),
                        null));
    }

    public boolean delete(Long id) {
        return gateway.deleteArticle(id);
    }

    public ArticleCategoryDTO categoryDetail(Long id) {
        return category(gateway.category(id));
    }

    public List<ArticleCategoryDTO> enabledCategories() {
        return gateway.enabledCategories().stream().map(ArticleService::category).toList();
    }

    public PageResult<ArticleCategoryDTO> categoryPage(ArticleCategoryPageQry q) {
        var result = gateway.categories(q.name(), q.status(), q.pageNum(), q.pageSize());
        return PageResult.of(
                result.getRecords().stream().map(ArticleService::category).toList(),
                result.getTotal());
    }

    public Long saveCategory(ArticleCategorySaveCmd cmd) {
        return gateway.saveCategory(
                new ArticleCategory(cmd.id(), cmd.name(), cmd.picUrl(), cmd.sort(), cmd.status()));
    }

    /** 已被文章引用的分类不能直接删除。 */
    public boolean deleteCategory(Long id) {
        return gateway.deleteCategory(id);
    }

    private static ArticleDTO article(Article v) {
        return v == null
                ? null
                : new ArticleDTO(
                        v.id(),
                        v.categoryId(),
                        v.spuId(),
                        v.title(),
                        v.author(),
                        v.picUrl(),
                        v.introduction(),
                        v.sort(),
                        v.status(),
                        v.recommendHot(),
                        v.recommendBanner(),
                        v.content(),
                        v.browseCount());
    }

    private static ArticleCategoryDTO category(ArticleCategory v) {
        return v == null
                ? null
                : new ArticleCategoryDTO(v.id(), v.name(), v.picUrl(), v.sort(), v.status());
    }
}
