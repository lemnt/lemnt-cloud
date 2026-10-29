package com.lemnt.mall.application.promotion.article.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.article.gateway.ArticleGateway;
import com.lemnt.mall.domain.promotion.article.gateway.ArticleCategoryGateway;
import com.lemnt.mall.domain.promotion.article.model.Article;
import com.lemnt.mall.domain.promotion.article.model.ArticleFilter;
import com.lemnt.mall.application.promotion.article.cmd.ArticleCreateCmd;
import com.lemnt.mall.application.promotion.article.cmd.ArticleUpdateCmd;
import com.lemnt.mall.application.promotion.article.qry.ArticlePageQry;
import com.lemnt.mall.application.promotion.article.dto.ArticleDTO;
import com.lemnt.mall.application.promotion.article.converter.ArticleDTOConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 商城文章用例编排，跨表校验不形成Service循环依赖。 */
@Component
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleGateway gateway;
    private final ArticleCategoryGateway categories;

    /** 新增商城文章，不接受调用方提供浏览次数。 */
    @Transactional(rollbackFor = Exception.class)
    public Long create(ArticleCreateCmd cmd) {
        ArticleValidation.id(cmd.categoryId());
        if (cmd.spuId() == null || cmd.spuId() < 0 || cmd.recommendHot() == null || cmd.recommendBanner() == null) {
            throw new ServiceException("商品关联或推荐状态不合法", 400);
        }
        ArticleValidation.text(cmd.title(), 255, true);
        ArticleValidation.text(cmd.author(), 128, false);
        ArticleValidation.text(cmd.picUrl(), 1024, true);
        ArticleValidation.text(cmd.introduction(), 2000, false);
        ArticleValidation.text(cmd.content(), 1000000, true);
        ArticleValidation.state(cmd.status(), cmd.sort());
        if (categories.lock(cmd.categoryId()) == null) {
            throw new ServiceException("文章分类不存在", 404);
        }
        return gateway.save(new Article(null, cmd.categoryId(), cmd.spuId(), cmd.title(), cmd.author(), cmd.picUrl(), cmd.introduction(), cmd.sort(), cmd.status(), cmd.recommendHot(), cmd.recommendBanner(), cmd.content(), 0, null)).id();
    }

    /** 更新商城文章，不接受调用方提供浏览次数。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean update(ArticleUpdateCmd cmd) {
        ArticleValidation.id(cmd.id());
        detail(cmd.id());
        ArticleValidation.id(cmd.categoryId());
        if (cmd.spuId() == null || cmd.spuId() < 0 || cmd.recommendHot() == null || cmd.recommendBanner() == null) {
            throw new ServiceException("商品关联或推荐状态不合法", 400);
        }
        ArticleValidation.text(cmd.title(), 255, true);
        ArticleValidation.text(cmd.author(), 128, false);
        ArticleValidation.text(cmd.picUrl(), 1024, true);
        ArticleValidation.text(cmd.introduction(), 2000, false);
        ArticleValidation.text(cmd.content(), 1000000, true);
        ArticleValidation.state(cmd.status(), cmd.sort());
        if (categories.lock(cmd.categoryId()) == null) {
            throw new ServiceException("文章分类不存在", 404);
        }
        gateway.save(new Article(cmd.id(), cmd.categoryId(), cmd.spuId(), cmd.title(), cmd.author(), cmd.picUrl(), cmd.introduction(), cmd.sort(), cmd.status(), cmd.recommendHot(), cmd.recommendBanner(), cmd.content(), 0, null));
        return true;
    }

    /** 校验存在后删除，关联文章的分类不可删除。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        ArticleValidation.id(id);
        var article = detail(id);
        categories.lock(article.categoryId());
        if (!gateway.delete(id)) {
            throw new ServiceException("商城文章不存在", 404);
        }
        return true;
    }

    /** 后台详情不存在报错。 */
    public ArticleDTO detail(Long id) {
        ArticleValidation.id(id);
        var row = gateway.find(id);
        if (row == null) {
            throw new ServiceException("商城文章不存在", 404);
        }
        return ArticleDTOConverter.toDTO(row);
    }

    /** 按显式条件分页，创建时间单侧边界仍有效。 */
    public PageResult<ArticleDTO> page(ArticlePageQry qry) {
        ArticleValidation.page(qry.pageNum(), qry.pageSize(), qry.createFrom(), qry.createTo());
        var page = gateway.page(new ArticleFilter(qry.categoryId(), qry.spuId(), qry.title(), qry.author(), qry.status(), qry.recommendHot(), qry.recommendBanner(), qry.createFrom(), qry.createTo(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(ArticleDTOConverter::toDTO).toList(), page.getTotal());
    }

    /** 按源推荐标记查询；false不丢弃，源未增加文章启用过滤。 */
    public java.util.List<ArticleDTO> recommended(Boolean hot, Boolean banner) {
        return gateway.recommended(hot, banner).stream().map(ArticleDTOConverter::toDTO).toList();
    }

    /** id优先，否则精确标题取最后一篇；无匹配返回null。 */
    public ArticleDTO read(Long id, String title) {
        if (id != null) {
            ArticleValidation.id(id);
            return ArticleDTOConverter.toDTO(gateway.find(id));
        }
        ArticleValidation.text(title, 255, true);
        return ArticleDTOConverter.toDTO(gateway.lastByTitle(title));
    }

    /** 独立增加浏览量，网络未知结果不自动重试。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean increment(Long id) {
        ArticleValidation.id(id);
        if (!gateway.incrementBrowseCount(id)) {
            throw new ServiceException("文章不存在", 404);
        }
        return true;
    }
}
