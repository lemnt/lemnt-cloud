package com.lemnt.mall.application.promotion.article.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.article.gateway.ArticleGateway;
import com.lemnt.mall.domain.promotion.article.gateway.ArticleCategoryGateway;
import com.lemnt.mall.domain.promotion.article.model.ArticleCategory;
import com.lemnt.mall.domain.promotion.article.model.ArticleCategoryFilter;
import com.lemnt.mall.application.promotion.article.cmd.ArticleCategoryCreateCmd;
import com.lemnt.mall.application.promotion.article.cmd.ArticleCategoryUpdateCmd;
import com.lemnt.mall.application.promotion.article.qry.ArticleCategoryPageQry;
import com.lemnt.mall.application.promotion.article.dto.ArticleCategoryDTO;
import com.lemnt.mall.application.promotion.article.converter.ArticleCategoryDTOConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 文章分类用例编排，跨表校验不形成Service循环依赖。 */
@Component
@RequiredArgsConstructor
public class ArticleCategoryService {
    private final ArticleCategoryGateway gateway;
    private final ArticleGateway articles;

    /** 新增文章分类，不接受调用方提供浏览次数。 */
    @Transactional(rollbackFor = Exception.class)
    public Long create(ArticleCategoryCreateCmd cmd) {
        ArticleValidation.text(cmd.name(), 128, true);
        ArticleValidation.text(cmd.picUrl(), 1024, false);
        ArticleValidation.state(cmd.status(), cmd.sort());
        return gateway.save(new ArticleCategory(null, cmd.name(), cmd.picUrl(), cmd.status(), cmd.sort(), null)).id();
    }

    /** 更新文章分类，不接受调用方提供浏览次数。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean update(ArticleCategoryUpdateCmd cmd) {
        ArticleValidation.id(cmd.id());
        detail(cmd.id());
        ArticleValidation.text(cmd.name(), 128, true);
        ArticleValidation.text(cmd.picUrl(), 1024, false);
        ArticleValidation.state(cmd.status(), cmd.sort());
        gateway.save(new ArticleCategory(cmd.id(), cmd.name(), cmd.picUrl(), cmd.status(), cmd.sort(), null));
        return true;
    }

    /** 校验存在后删除，关联文章的分类不可删除。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        ArticleValidation.id(id);
        if (gateway.lock(id) == null) {
            throw new ServiceException("文章分类不存在", 404);
        }
        if (articles.countByCategory(id) > 0) {
            throw new ServiceException("分类下存在文章，不能删除", 409);
        }
        if (!gateway.delete(id)) {
            throw new ServiceException("文章分类不存在", 404);
        }
        return true;
    }

    /** 后台详情不存在报错。 */
    public ArticleCategoryDTO detail(Long id) {
        ArticleValidation.id(id);
        var row = gateway.find(id);
        if (row == null) {
            throw new ServiceException("文章分类不存在", 404);
        }
        return ArticleCategoryDTOConverter.toDTO(row);
    }

    /** 按显式条件分页，创建时间单侧边界仍有效。 */
    public PageResult<ArticleCategoryDTO> page(ArticleCategoryPageQry qry) {
        ArticleValidation.page(qry.pageNum(), qry.pageSize(), qry.createFrom(), qry.createTo());
        var page = gateway.page(new ArticleCategoryFilter(qry.name(), qry.status(), qry.createFrom(), qry.createTo(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(ArticleCategoryDTOConverter::toDTO).toList(), page.getTotal());
    }

    /** App启用分类，按sort升序。 */
    public java.util.List<ArticleCategoryDTO> enabled() {
        return gateway.enabled().stream().map(ArticleCategoryDTOConverter::toDTO).toList();
    }
}
