package com.lemnt.mall.adapter.app.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.mall.adapter.app.product.req.AppProductFavoriteCreateReq;
import com.lemnt.mall.adapter.app.product.req.AppProductFavoriteExistsReq;
import com.lemnt.mall.adapter.app.product.req.AppProductFavoritePageReq;
import com.lemnt.mall.adapter.app.product.res.AppProductFavoriteCountRes;
import com.lemnt.mall.adapter.app.product.res.AppProductFavoriteCreateRes;
import com.lemnt.mall.adapter.app.product.res.AppProductFavoriteDeleteRes;
import com.lemnt.mall.adapter.app.product.res.AppProductFavoriteExistsRes;
import com.lemnt.mall.adapter.app.product.res.AppProductFavoritePageRes;
import com.lemnt.mall.adapter.app.product.converter.ProductFavoriteReqConverter;
import com.lemnt.mall.adapter.app.product.converter.ProductFavoriteResConverter;
import com.lemnt.mall.application.product.cmd.ProductFavoriteDeleteCmd;
import com.lemnt.mall.application.product.qry.ProductFavoriteCountQry;
import com.lemnt.mall.application.product.qry.ProductFavoriteExistsQry;
import com.lemnt.mall.application.product.executor.ProductFavoriteCountQueryExe;
import com.lemnt.mall.application.product.executor.ProductFavoriteCreateCmdExe;
import com.lemnt.mall.application.product.executor.ProductFavoriteDeleteCmdExe;
import com.lemnt.mall.application.product.executor.ProductFavoriteExistsQueryExe;
import com.lemnt.mall.application.product.executor.ProductFavoritePageQueryExe;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 当前会员商品收藏，Token认证统一在网关。 */
@RestController
@RequestMapping("/mall/app/favorites")
@RequiredArgsConstructor
public class AppProductFavoriteController {
    private final ProductFavoriteCreateCmdExe create;
    private final ProductFavoritePageQueryExe query;
    private final ProductFavoriteDeleteCmdExe delete;
    private final ProductFavoriteExistsQueryExe exists;
    private final ProductFavoriteCountQueryExe count;

    /** 登录后的独立写入，不接受userId参数。 */
    @PostMapping
    @Operation(summary = "收藏商品")
    public R<AppProductFavoriteCreateRes> create(@Valid @RequestBody AppProductFavoriteCreateReq req) {
        return R.ok(new AppProductFavoriteCreateRes(create.execute(ProductFavoriteReqConverter.create(member(), req))));
    }

    /** 本人分页，不能通过Query伪造其他会员。 */
    @GetMapping
    @Operation(summary = "本人收藏分页")
    public R<AppProductFavoritePageRes> page(@Valid @ModelAttribute AppProductFavoritePageReq req) {
        var result = query.execute(ProductFavoriteReqConverter.page(member(), req));
        return R.page(result.getRecords().stream().map(ProductFavoriteResConverter::page).toList(), result.getTotal());
    }

    /** 取消只根据当前会员及商品匹配。 */
    @DeleteMapping("/{spuId:[1-9][0-9]*}")
    @Operation(summary = "取消本人商品收藏")
    public R<AppProductFavoriteDeleteRes> delete(@PathVariable Long spuId) {
        return R.ok(new AppProductFavoriteDeleteRes(delete.execute(new ProductFavoriteDeleteCmd(member(), spuId))));
    }

    /** 状态查询用于未知写入结果后核验。 */
    @GetMapping("/exists")
    @Operation(summary = "查询本人收藏状态")
    public R<AppProductFavoriteExistsRes> exists(@Valid @ModelAttribute AppProductFavoriteExistsReq req) {
        return R.ok(new AppProductFavoriteExistsRes(exists.execute(new ProductFavoriteExistsQry(member(), req.spuId()))));
    }

    /** 本人收藏数量。 */
    @GetMapping("/count")
    @Operation(summary = "本人收藏数量")
    public R<AppProductFavoriteCountRes> count() {
        return R.ok(new AppProductFavoriteCountRes(count.execute(new ProductFavoriteCountQry(member()))));
    }

    private Long member() {
        var id = SecurityUtils.getUserId();
        if (id == null || id <= 0) {
            throw new ServiceException("请先登录", 401);
        }
        return id;
    }
}
