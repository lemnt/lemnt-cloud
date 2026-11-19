package com.lemnt.promotion.domain.article.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.promotion.domain.article.model.Article;
import com.lemnt.promotion.domain.article.model.ArticleCategory;

import java.util.List;

/** 文章和分类的持久化边界，公开查询在数据库侧限制为启用状态。 */
public interface ArticleGateway {
    Article find(Long id);

    Article published(Long id, String title);

    PageResult<Article> articles(
            String title,
            Long categoryId,
            Integer status,
            Boolean hot,
            Boolean banner,
            boolean published,
            int pageNum,
            int pageSize);

    Long save(Article article);

    boolean deleteArticle(Long id);

    boolean addBrowse(Long id);

    ArticleCategory category(Long id);

    PageResult<ArticleCategory> categories(String name, Integer status, int pageNum, int pageSize);

    List<ArticleCategory> enabledCategories();

    Long saveCategory(ArticleCategory category);

    boolean deleteCategory(Long id);
}
