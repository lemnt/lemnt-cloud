package com.lemnt.mall.adapter.app.promotion.article;

import com.lemnt.common.core.domain.R;
import com.lemnt.mall.adapter.app.promotion.article.req.*;
import com.lemnt.mall.adapter.app.promotion.article.res.*;
import com.lemnt.mall.adapter.app.promotion.article.converter.AppArticleReqConverter;
import com.lemnt.mall.adapter.app.promotion.article.converter.AppArticleResConverter;
import com.lemnt.mall.application.promotion.article.executor.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 公开文章查询及独立浏览计数，保持源文章可见性规则。 */
@RestController
@RequestMapping("/mall/app/articles")
@RequiredArgsConstructor
public class AppArticleController {
    private final ArticlePageQueryExe page;
    private final ArticleReadQueryExe read;
    private final ArticleRecommendedListQueryExe recommended;
    private final ArticleBrowseCountCmdExe browse;

    /** 按分类分页。 */
    @GetMapping
    @Operation(summary = "商城文章分页")
    public R<AppArticlePageRes> page(@Valid @ModelAttribute AppArticlePageReq req) {
        var result = page.execute(AppArticleReqConverter.page(req));
        return R.page(result.getRecords().stream().map(AppArticleResConverter::page).toList(), result.getTotal());
    }

    /** 热门/轮播筛选，false有效。 */
    @GetMapping("/recommended")
    @Operation(summary = "推荐文章列表")
    public R<java.util.List<AppArticleRecommendedListRes>> recommended(@Valid @ModelAttribute AppArticleRecommendedListReq req) {
        return R.ok(recommended.execute(AppArticleReqConverter.recommended(req)).stream()
                .map(AppArticleResConverter::recommendedList).toList());
    }

    /** 详情不隐式增加浏览次数。 */
    @GetMapping("/detail")
    @Operation(summary = "按编号或标题读取文章")
    public R<AppArticleDetailRes> detail(@Valid @ModelAttribute AppArticleDetailReq req) {
        return R.ok(AppArticleResConverter.detail(read.execute(AppArticleReqConverter.detail(req))));
    }

    /** 浏览增量调用后未知结果不能自动重试。 */
    @PutMapping("/{id:[1-9][0-9]*}/browse-count")
    @Operation(summary = "增加文章浏览次数")
    public R<AppArticleBrowseCountRes> browse(@PathVariable Long id) {
        return R.ok(new AppArticleBrowseCountRes(browse.execute(id)));
    }
}
