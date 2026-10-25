package com.lemnt.trade.adapter.web.checkout;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.checkout.req.*;
import com.lemnt.trade.adapter.web.checkout.res.*;
import com.lemnt.trade.adapter.web.checkout.converter.*;
import com.lemnt.trade.application.checkout.executor.*;
import com.lemnt.trade.application.checkout.cmd.*;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Tag(name = "后台会员购物车管理")
@RestController
@RequestMapping("/trade/admin/members/{memberId}/cart")
@RequiredArgsConstructor
public class AdminCartController {
    private final CartListQueryExe list;
    private final CartSaveCmdExe save;
    private final CartDeleteCmdExe delete;

    @Operation(summary = "查看目标会员购物车，刷新服务器商品信息")
    @GetMapping
    @RequiresPermissions("trade:cart:query")
    public R<List<CartItemRes>> list(@PathVariable Long memberId) {
        return R.ok(list.execute(memberId).stream().map(CheckoutResConverter::cartItem).toList());
    }

    @Operation(summary = "设置目标会员SKU数量及选中状态，不预占库存")
    @PutMapping("/{productId}")
    @RequiresPermissions("trade:cart:edit")
    public R<CartItemRes> save(
            @PathVariable Long memberId,
            @PathVariable Long productId,
            @Valid @RequestBody CartSaveReq req) {
        return R.ok(
                CheckoutResConverter.cartItem(
                        save.execute(CheckoutReqConverter.cart(memberId, productId, req))));
    }

    @Operation(summary = "删除目标会员购物车条目")
    @DeleteMapping("/{productId}")
    @RequiresPermissions("trade:cart:edit")
    public R<Boolean> delete(@PathVariable Long memberId, @PathVariable Long productId) {
        return R.ok(delete.execute(new CartDeleteCmd(memberId, productId)));
    }
}
