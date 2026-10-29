package com.lemnt.mall.infrastructure.promotion.article.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.domain.promotion.article.gateway.ArticleCategoryGateway;
import com.lemnt.mall.domain.promotion.article.model.ArticleCategory;
import com.lemnt.mall.domain.promotion.article.model.ArticleCategoryFilter;
import com.lemnt.mall.infrastructure.promotion.article.dataobject.ArticleCategoryDO;
import com.lemnt.mall.infrastructure.promotion.article.mapper.ArticleCategoryMapper;
import com.lemnt.mall.infrastructure.promotion.article.converter.ArticleCategoryConverter;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 文章分类表存储实现。 */
@Component
@RequiredArgsConstructor
public class ArticleCategoryGatewayImpl implements ArticleCategoryGateway {
    private final ArticleCategoryMapper mapper;
    @Override
    public ArticleCategory find(Long id) {
        return ArticleCategoryConverter.toModel(mapper.selectById(id));
    }

    @Override
    public ArticleCategory save(ArticleCategory value) {
        var row = ArticleCategoryConverter.toDO(value);
        if (value.id() == null) {
            mapper.insert(row);
        } else if (mapper.update(null, Wrappers.lambdaUpdate(ArticleCategoryDO.class)
                    .eq(ArticleCategoryDO::getId, value.id())
                    .set(ArticleCategoryDO::getName, value.name())
                    .set(ArticleCategoryDO::getPicUrl, value.picUrl())
                    .set(ArticleCategoryDO::getStatus, value.status())
                    .set(ArticleCategoryDO::getSort, value.sort())) != 1) {
            throw new ServiceException("文章分类不存在", 404);
        }
        return find(row.getId());
    }

    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) == 1;
    }

    @Override
    public PageResult<ArticleCategory> page(ArticleCategoryFilter filter) {
        var page = mapper.selectPage(new Page<>(filter.pageNum(), filter.pageSize()), Wrappers.lambdaQuery(ArticleCategoryDO.class)
                .like(filter.name() != null, ArticleCategoryDO::getName, filter.name())
                .eq(filter.status() != null, ArticleCategoryDO::getStatus, filter.status())
                .ge(filter.createFrom() != null, ArticleCategoryDO::getCreateTime, filter.createFrom())
                .le(filter.createTo() != null, ArticleCategoryDO::getCreateTime, filter.createTo())
                .orderByDesc(ArticleCategoryDO::getSort));
        return PageResult.of(page.getRecords().stream().map(ArticleCategoryConverter::toModel).toList(), page.getTotal());
    }

    @Override
    public ArticleCategory lock(Long id) {
        return ArticleCategoryConverter.toModel(mapper.lock(id));
    }

    @Override
    public java.util.List<ArticleCategory> enabled() {
        return mapper.selectList(Wrappers.lambdaQuery(ArticleCategoryDO.class)
                .eq(ArticleCategoryDO::getStatus, 0).orderByAsc(ArticleCategoryDO::getSort)
                .orderByAsc(ArticleCategoryDO::getId)).stream().map(ArticleCategoryConverter::toModel).toList();
    }
}
