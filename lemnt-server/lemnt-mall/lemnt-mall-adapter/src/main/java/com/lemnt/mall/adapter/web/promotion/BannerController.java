package com.lemnt.mall.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.promotion.converter.BannerConverter;
import com.lemnt.mall.adapter.web.promotion.req.BannerCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.BannerPageReq;
import com.lemnt.mall.adapter.web.promotion.req.BannerUpdateReq;
import com.lemnt.mall.adapter.web.promotion.res.BannerRes;
import com.lemnt.mall.application.promotion.executor.BannerCreateCmdExe;
import com.lemnt.mall.application.promotion.executor.BannerDeleteCmdExe;
import com.lemnt.mall.application.promotion.executor.BannerDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.BannerPageQueryExe;
import com.lemnt.mall.application.promotion.executor.BannerUpdateCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 后台 Banner 管理。 */
@Tag(name = "Banner 管理")
@RestController
@RequestMapping("/mall/promotion/banners")
@RequiredArgsConstructor
public class BannerController {
    private final BannerCreateCmdExe create;
    private final BannerUpdateCmdExe update;
    private final BannerDeleteCmdExe delete;
    private final BannerDetailQueryExe detail;
    private final BannerPageQueryExe page;

    @PostMapping
    @Operation(summary = "新增 Banner")
    @RequiresPermissions("mall:promotion:create")
    public R<Long> create(@Valid @RequestBody BannerCreateReq req) {
        return R.ok(create.execute(BannerConverter.create(req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改 Banner")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody BannerUpdateReq req) {
        return R.ok(update.execute(BannerConverter.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除 Banner")
    @RequiresPermissions("mall:promotion:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Banner 详情")
    @RequiresPermissions("mall:promotion:query")
    public R<BannerRes> detail(@PathVariable Long id) {
        return R.ok(BannerConverter.res(detail.execute(id)));
    }

    @GetMapping
    @Operation(summary = "Banner 分页")
    @RequiresPermissions("mall:promotion:list")
    public R<BannerRes> page(@Valid @ModelAttribute BannerPageReq req) {
        var result = page.execute(BannerConverter.page(req));
        return R.page(
                result.getRecords().stream().map(BannerConverter::res).toList(), result.getTotal());
    }
}
