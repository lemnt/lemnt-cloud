package com.lemnt.mall.adapter.app.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.mall.adapter.app.promotion.converter.AppBannerConverter;
import com.lemnt.mall.adapter.app.promotion.res.AppBannerRes;
import com.lemnt.mall.application.promotion.executor.AppBannerListQueryExe;
import com.lemnt.mall.application.promotion.executor.BannerBrowseCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序 Banner 展示。 */
@Tag(name = "小程序 Banner")
@RestController
@RequestMapping("/mall/app/banners")
@RequiredArgsConstructor
public class AppBannerController {
    private final AppBannerListQueryExe list;
    private final BannerBrowseCmdExe browse;

    @GetMapping
    @Operation(summary = "按位置查询开启的 Banner")
    public R<List<AppBannerRes>> list(@RequestParam(defaultValue = "1") Integer position) {
        return R.ok(list.execute(position).stream().map(AppBannerConverter::banner).toList());
    }

    @PutMapping("/{id}/browse")
    @Operation(summary = "累加 Banner 点击量")
    public R<Boolean> browse(@PathVariable Long id) {
        return R.ok(browse.execute(id));
    }
}
