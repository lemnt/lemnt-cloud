package com.lemnt.mall.adapter.app.promotion.article;

import com.lemnt.common.core.domain.R;
import com.lemnt.mall.adapter.app.promotion.article.res.AppArticleCategoryListRes;
import com.lemnt.mall.application.promotion.article.executor.ArticleCategoryEnabledListQueryExe;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 公开启用文章分类列表。 */
@RestController
@RequestMapping("/mall/app/article-categories")
@RequiredArgsConstructor
public class AppArticleCategoryController {
    private final ArticleCategoryEnabledListQueryExe query;
    /** 按源sort升序返回启用分类。 */
    @GetMapping
    @Operation(summary = "启用文章分类")
    public R<java.util.List<AppArticleCategoryListRes>> list() {
        return R.ok(query.execute().stream().map(value ->
                new AppArticleCategoryListRes(value.id(), value.name(), value.picUrl())).toList());
    }
}
