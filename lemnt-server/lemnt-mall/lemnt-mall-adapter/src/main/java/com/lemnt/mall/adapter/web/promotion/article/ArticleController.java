package com.lemnt.mall.adapter.web.promotion.article;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.promotion.article.req.*;
import com.lemnt.mall.adapter.web.promotion.article.res.*;
import com.lemnt.mall.adapter.web.promotion.article.converter.ArticleReqConverter;
import com.lemnt.mall.adapter.web.promotion.article.converter.ArticleResConverter;
import com.lemnt.mall.application.promotion.article.executor.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 商城文章管理接口。 */
@RestController
@RequestMapping("/mall/promotion/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleCreateCmdExe create;
    private final ArticleUpdateCmdExe update;
    private final ArticleDeleteCmdExe delete;
    private final ArticleDetailQueryExe detail;
    private final ArticlePageQueryExe page;

    /** 分页查询。 */
    @GetMapping
    @Operation(summary = "商城文章分页")
    @RequiresPermissions("mall:article:list")
    public R<ArticlePageRes> page(@Valid @ModelAttribute ArticlePageReq req) {
        var result = page.execute(ArticleReqConverter.page(req));
        return R.page(result.getRecords().stream().map(ArticleResConverter::page).toList(), result.getTotal());
    }

    /** 查询详情。 */
    @GetMapping("/{id:[1-9][0-9]*}")
    @Operation(summary = "商城文章详情")
    @RequiresPermissions("mall:article:query")
    public R<ArticleDetailRes> detail(@PathVariable Long id) {
        return R.ok(ArticleResConverter.detail(detail.execute(id)));
    }

    /** 新增记录。 */
    @PostMapping
    @Operation(summary = "新增商城文章")
    @RequiresPermissions("mall:article:create")
    public R<ArticleCreateRes> create(@Valid @RequestBody ArticleCreateReq req) {
        return R.ok(new ArticleCreateRes(create.execute(ArticleReqConverter.create(req))));
    }

    /** 修改资料，不接受浏览次数。 */
    @PutMapping("/{id:[1-9][0-9]*}")
    @Operation(summary = "修改商城文章")
    @RequiresPermissions("mall:article:update")
    public R<ArticleUpdateRes> update(@PathVariable Long id, @Valid @RequestBody ArticleUpdateReq req) {
        return R.ok(new ArticleUpdateRes(update.execute(ArticleReqConverter.update(id, req))));
    }

    /** 删除记录。 */
    @DeleteMapping("/{id:[1-9][0-9]*}")
    @Operation(summary = "删除商城文章")
    @RequiresPermissions("mall:article:delete")
    public R<ArticleDeleteRes> delete(@PathVariable Long id) {
        return R.ok(new ArticleDeleteRes(delete.execute(id)));
    }

}
