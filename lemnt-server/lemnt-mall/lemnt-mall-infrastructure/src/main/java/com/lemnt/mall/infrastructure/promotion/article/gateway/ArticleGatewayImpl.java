package com.lemnt.mall.infrastructure.promotion.article.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.domain.promotion.article.gateway.ArticleGateway;
import com.lemnt.mall.domain.promotion.article.model.Article;
import com.lemnt.mall.domain.promotion.article.model.ArticleFilter;
import com.lemnt.mall.infrastructure.promotion.article.dataobject.ArticleDO;
import com.lemnt.mall.infrastructure.promotion.article.mapper.ArticleMapper;
import com.lemnt.mall.infrastructure.promotion.article.converter.ArticleConverter;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 商城文章表存储实现。 */
@Component
@RequiredArgsConstructor
public class ArticleGatewayImpl implements ArticleGateway {
    private final ArticleMapper mapper;
    @Override
    public Article find(Long id) {
        return ArticleConverter.toModel(mapper.selectById(id));
    }

    @Override
    public Article save(Article value) {
        var row = ArticleConverter.toDO(value);
        if (value.id() == null) {
            mapper.insert(row);
        } else if (mapper.update(null, Wrappers.lambdaUpdate(ArticleDO.class)
                    .eq(ArticleDO::getId, value.id())
                    .set(ArticleDO::getCategoryId, value.categoryId())
                    .set(ArticleDO::getSpuId, value.spuId())
                    .set(ArticleDO::getTitle, value.title())
                    .set(ArticleDO::getAuthor, value.author())
                    .set(ArticleDO::getPicUrl, value.picUrl())
                    .set(ArticleDO::getIntroduction, value.introduction())
                    .set(ArticleDO::getSort, value.sort())
                    .set(ArticleDO::getStatus, value.status())
                    .set(ArticleDO::getRecommendHot, value.recommendHot())
                    .set(ArticleDO::getRecommendBanner, value.recommendBanner())
                    .set(ArticleDO::getContent, value.content())) != 1) {
            throw new ServiceException("商城文章不存在", 404);
        }
        return find(row.getId());
    }

    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) == 1;
    }

    @Override
    public PageResult<Article> page(ArticleFilter filter) {
        var page = mapper.selectPage(new Page<>(filter.pageNum(), filter.pageSize()), Wrappers.lambdaQuery(ArticleDO.class)
                .eq(filter.categoryId() != null, ArticleDO::getCategoryId, filter.categoryId())
                .eq(filter.spuId() != null, ArticleDO::getSpuId, filter.spuId())
                .eq(filter.title() != null, ArticleDO::getTitle, filter.title())
                .eq(filter.author() != null, ArticleDO::getAuthor, filter.author())
                .eq(filter.status() != null, ArticleDO::getStatus, filter.status())
                .eq(filter.recommendHot() != null, ArticleDO::getRecommendHot, filter.recommendHot())
                .eq(filter.recommendBanner() != null, ArticleDO::getRecommendBanner, filter.recommendBanner())
                .ge(filter.createFrom() != null, ArticleDO::getCreateTime, filter.createFrom())
                .le(filter.createTo() != null, ArticleDO::getCreateTime, filter.createTo())
                .orderByDesc(ArticleDO::getId));
        return PageResult.of(page.getRecords().stream().map(ArticleConverter::toModel).toList(), page.getTotal());
    }

    @Override
    public long countByCategory(Long categoryId) {
        return mapper.selectCount(Wrappers.lambdaQuery(ArticleDO.class).eq(ArticleDO::getCategoryId, categoryId));
    }

    @Override
    public java.util.List<Article> recommended(Boolean hot, Boolean banner) {
        return mapper.selectList(Wrappers.lambdaQuery(ArticleDO.class)
                .eq(hot != null, ArticleDO::getRecommendHot, hot)
                .eq(banner != null, ArticleDO::getRecommendBanner, banner)
                .orderByAsc(ArticleDO::getId)).stream().map(ArticleConverter::toModel).toList();
    }

    @Override
    public Article lastByTitle(String title) {
        var rows = mapper.selectPage(new Page<>(1, 1, false), Wrappers.lambdaQuery(ArticleDO.class)
                .eq(ArticleDO::getTitle, title).orderByDesc(ArticleDO::getId)).getRecords();
        return rows.isEmpty() ? null : ArticleConverter.toModel(rows.get(0));
    }

    @Override
    public boolean incrementBrowseCount(Long id) {
        return mapper.update(null, Wrappers.lambdaUpdate(ArticleDO.class)
                .eq(ArticleDO::getId, id).setIncrBy(ArticleDO::getBrowseCount, 1)) == 1;
    }
}
