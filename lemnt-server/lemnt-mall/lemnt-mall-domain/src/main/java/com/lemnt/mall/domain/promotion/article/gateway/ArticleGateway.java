package com.lemnt.mall.domain.promotion.article.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.article.model.Article;
import com.lemnt.mall.domain.promotion.article.model.ArticleFilter;

/** 商城文章按表领域存储。 */
public interface ArticleGateway {
    /** 查询有效记录。 */
    Article find(Long id);
    /** 保存资料，更新不改浏览次数。 */
    Article save(Article value);
    /** 逻辑删除。 */
    boolean delete(Long id);
    /** 分页。 */
    PageResult<Article> page(ArticleFilter filter);
    /** 分类有效文章计数，删除分类前检查。 */
    long countByCategory(Long categoryId);
    /** 推荐列表，false为有效筛选。 */
    java.util.List<Article> recommended(Boolean hot, Boolean banner);
    /** 同标题最后一篇。 */
    Article lastByTitle(String title);
    /** 原子增加实际浏览次数。 */
    boolean incrementBrowseCount(Long id);
}
