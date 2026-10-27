package com.lemnt.mall.adapter.web.checkout;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.checkout.converter.CheckoutReqConverter;
import com.lemnt.mall.adapter.web.checkout.converter.CheckoutResConverter;
import com.lemnt.mall.adapter.web.checkout.req.CartSaveReq;
import com.lemnt.mall.adapter.web.checkout.res.MallCartListRes;
import com.lemnt.mall.adapter.web.checkout.res.MallCartSaveRes;
import com.lemnt.mall.application.checkout.cmd.CartDeleteCmd;
import com.lemnt.mall.application.checkout.executor.CartDeleteCmdExe;
import com.lemnt.mall.application.checkout.executor.CartListQueryExe;
import com.lemnt.mall.application.checkout.executor.CartSaveCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 后台会员购物车管理。
 */
@Tag(name = "后台会员购物车管理")
@RestController
@RequestMapping("/mall/admin/members/{memberId}/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartListQueryExe cartListQueryExe;
    private final CartSaveCmdExe cartSaveCmdExe;
    private final CartDeleteCmdExe cartDeleteCmdExe;

    @Operation(summary = "查看目标会员购物车，刷新服务器商品信息")
    @GetMapping
    @RequiresPermissions("mall:cart:query")
    public R<List<MallCartListRes>> list(@PathVariable Long memberId) {
        return R.ok(cartListQueryExe.execute(memberId).stream().map(CheckoutResConverter::cartItemToMallCartListRes).toList());
    }

    @Operation(summary = "设置目标会员SKU数量及选中状态，不预占库存")
    @PutMapping("/{productId}")
    @RequiresPermissions("mall:cart:edit")
    public R<MallCartSaveRes> save(
            @PathVariable Long memberId,
            @PathVariable Long productId,
            @Valid @RequestBody CartSaveReq req) {
        return R.ok(
                CheckoutResConverter.cartItemToMallCartSaveRes(
                cartSaveCmdExe.execute(CheckoutReqConverter.cart(memberId, productId, req))));
    }

    @Operation(summary = "删除目标会员购物车条目")
    @DeleteMapping("/{productId}")
    @RequiresPermissions("mall:cart:edit")
    public R<Boolean> delete(@PathVariable Long memberId, @PathVariable Long productId) {
        return R.ok(cartDeleteCmdExe.execute(new CartDeleteCmd(memberId, productId)));
    }
}
