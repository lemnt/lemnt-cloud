package com.lemnt.mall.adapter.app.checkout;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.mall.adapter.app.checkout.converter.CheckoutReqConverter;
import com.lemnt.mall.adapter.app.checkout.converter.CheckoutResConverter;
import com.lemnt.mall.adapter.app.checkout.req.CartSaveReq;
import com.lemnt.mall.adapter.app.checkout.res.MallAppCartCartRes;
import com.lemnt.mall.adapter.app.checkout.res.MallAppCartSaveRes;
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

/** 小程序购物车接口。 */
@Tag(name = "小程序购物车")
@RestController
@RequestMapping("/mall/app")
@RequiredArgsConstructor
public class AppCartController {
    private final CartListQueryExe cartListQueryExe;
    private final CartSaveCmdExe cartSaveCmdExe;
    private final CartDeleteCmdExe cartDeleteCmdExe;

    @GetMapping("/cart")
    @Operation(summary = "查询当前会员购物车")
    public R<List<MallAppCartCartRes>> cart() {
        Long memberId = SecurityUtils.getUserId();
        return R.ok(cartListQueryExe.execute(memberId).stream().map(CheckoutResConverter::cartItemToMallAppCartCartRes).toList());
    }

    @PutMapping("/cart/{productId}")
    @Operation(summary = "更新当前会员购物车，不接受客户端价格或会员身份")
    public R<MallAppCartSaveRes> save(
            @PathVariable Long productId,
            @Valid @RequestBody CartSaveReq req) {
        Long memberId = SecurityUtils.getUserId();
        return R.ok(
                CheckoutResConverter.cartItemToMallAppCartSaveRes(
                cartSaveCmdExe.execute(CheckoutReqConverter.cart(memberId, productId, req))));
    }

    @DeleteMapping("/cart/{productId}")
    @Operation(summary = "删除当前会员购物车条目")
    public R<Boolean> delete(
            @PathVariable Long productId) {
        Long memberId = SecurityUtils.getUserId();
        return R.ok(cartDeleteCmdExe.execute(new CartDeleteCmd(memberId, productId)));
    }
}
