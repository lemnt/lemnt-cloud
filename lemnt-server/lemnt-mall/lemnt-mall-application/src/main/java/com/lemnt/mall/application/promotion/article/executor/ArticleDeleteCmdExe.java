package com.lemnt.mall.application.promotion.article.executor;

import com.lemnt.mall.application.promotion.article.service.ArticleService;
import com.lemnt.mall.application.promotion.article.cmd.*;
import com.lemnt.mall.application.promotion.article.qry.*;
import com.lemnt.mall.application.promotion.article.dto.*;
import com.lemnt.common.domain.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 商城文章用例执行入口。 */
@Component
@RequiredArgsConstructor
public class ArticleDeleteCmdExe {
    private final ArticleService service;
    /** 执行当前用例。 */
    public boolean execute(Long id) {
        return service.delete(id);
    }
}
