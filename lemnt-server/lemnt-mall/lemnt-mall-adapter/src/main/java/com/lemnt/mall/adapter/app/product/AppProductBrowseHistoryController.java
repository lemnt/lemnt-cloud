package com.lemnt.mall.adapter.app.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.mall.adapter.app.product.req.AppProductBrowseHistoryCreateReq;
import com.lemnt.mall.adapter.app.product.req.AppProductBrowseHistoryDeleteReq;
import com.lemnt.mall.adapter.app.product.req.AppProductBrowseHistoryPageReq;
import com.lemnt.mall.adapter.app.product.res.AppProductBrowseHistoryClearRes;
import com.lemnt.mall.adapter.app.product.res.AppProductBrowseHistoryCreateRes;
import com.lemnt.mall.adapter.app.product.res.AppProductBrowseHistoryDeleteRes;
import com.lemnt.mall.adapter.app.product.res.AppProductBrowseHistoryPageRes;
import com.lemnt.mall.adapter.app.product.converter.ProductBrowseHistoryReqConverter;
import com.lemnt.mall.adapter.app.product.converter.ProductBrowseHistoryResConverter;
import com.lemnt.mall.application.product.cmd.ProductBrowseHistoryClearCmd;
import com.lemnt.mall.application.product.cmd.ProductBrowseHistoryDeleteCmd;

import com.lemnt.mall.application.product.executor.ProductBrowseHistoryClearCmdExe;
import com.lemnt.mall.application.product.executor.ProductBrowseHistoryCreateCmdExe;
import com.lemnt.mall.application.product.executor.ProductBrowseHistoryDeleteCmdExe;
import com.lemnt.mall.application.product.executor.ProductBrowseHistoryPageQueryExe;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 当前会员浏览足迹，Token认证统一在网关。 */
@RestController
@RequestMapping("/mall/app/browse-history")
@RequiredArgsConstructor
public class AppProductBrowseHistoryController {
    private final ProductBrowseHistoryCreateCmdExe create;
    private final ProductBrowseHistoryPageQueryExe query;
    private final ProductBrowseHistoryDeleteCmdExe delete;
    private final ProductBrowseHistoryClearCmdExe clear;

    /** 登录后的独立写入，不接受userId参数。 */
    @PostMapping
    @Operation(summary = "记录本人最近浏览")
    public R<AppProductBrowseHistoryCreateRes> create(@Valid @RequestBody AppProductBrowseHistoryCreateReq req) {
        return R.ok(new AppProductBrowseHistoryCreateRes(create.execute(ProductBrowseHistoryReqConverter.create(member(), req))));
    }

    /** 本人分页，不能通过Query伪造其他会员。 */
    @GetMapping
    @Operation(summary = "本人足迹分页")
    public R<AppProductBrowseHistoryPageRes> page(@Valid @ModelAttribute AppProductBrowseHistoryPageReq req) {
        var result = query.execute(ProductBrowseHistoryReqConverter.page(member(), req));
        return R.page(result.getRecords().stream().map(ProductBrowseHistoryResConverter::page).toList(), result.getTotal());
    }

    /** 空数组不能隐式清空。 */
    @DeleteMapping
    @Operation(summary = "隐藏本人指定商品足迹")
    public R<AppProductBrowseHistoryDeleteRes> delete(@Valid @RequestBody AppProductBrowseHistoryDeleteReq req) {
        return R.ok(new AppProductBrowseHistoryDeleteRes(delete.execute(new ProductBrowseHistoryDeleteCmd(member(), req.spuIds()))));
    }

    /** 显式清空当前会员足迹，保留后台事实。 */
    @DeleteMapping("/clear")
    @Operation(summary = "清空本人足迹")
    public R<AppProductBrowseHistoryClearRes> clear() {
        return R.ok(new AppProductBrowseHistoryClearRes(clear.execute(new ProductBrowseHistoryClearCmd(member()))));
    }

    private Long member() {
        var id = SecurityUtils.getUserId();
        if (id == null || id <= 0) {
            throw new ServiceException("请先登录", 401);
        }
        return id;
    }
}
