package com.lemnt.mall.application.promotion.article.executor;

import com.lemnt.mall.application.promotion.article.service.ArticleCategoryService;
import com.lemnt.mall.application.promotion.article.cmd.*;
import com.lemnt.mall.application.promotion.article.qry.*;
import com.lemnt.mall.application.promotion.article.dto.*;
import com.lemnt.common.domain.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 文章分类用例执行入口。 */
@Component
@RequiredArgsConstructor
public class ArticleCategoryPageQueryExe {
    private final ArticleCategoryService service;
    /** 执行当前用例。 */
    public PageResult<ArticleCategoryDTO> execute(ArticleCategoryPageQry qry) {
        return service.page(qry);
    }
}
