package com.lemnt.mall.domain.promotion.article.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.article.model.ArticleCategory;
import com.lemnt.mall.domain.promotion.article.model.ArticleCategoryFilter;

/** 文章分类按表领域存储。 */
public interface ArticleCategoryGateway {
    /** 查询有效记录。 */
    ArticleCategory find(Long id);
    /** 保存资料，更新不改浏览次数。 */
    ArticleCategory save(ArticleCategory value);
    /** 逻辑删除。 */
    boolean delete(Long id);
    /** 分页。 */
    PageResult<ArticleCategory> page(ArticleCategoryFilter filter);
    /** 分类行锁。 */
    ArticleCategory lock(Long id);
    /** 按启用状态及排序读取。 */
    java.util.List<ArticleCategory> enabled();
}
