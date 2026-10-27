package com.lemnt.mall.adapter.app.store;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.mall.adapter.app.store.converter.PromotionReqConverter;
import com.lemnt.mall.adapter.app.store.converter.PromotionResConverter;
import com.lemnt.mall.adapter.app.store.req.CartAddReq;
import com.lemnt.mall.adapter.app.store.res.CartAdditionRes;
import com.lemnt.mall.application.store.executor.CartAddCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 小程序增量加购接口。 */
@Tag(name = "小程序增量加购")
@RestController
@RequestMapping("/mall/app")
@RequiredArgsConstructor
public class AppCartAdditionController {
    private final CartAddCmdExe cartAddCmdExe;

    @PostMapping("/cart/{productId}/add")
    @Operation(summary = "会员范围内幂等增量加购，返回首次结果快照")
    public R<CartAdditionRes> add(
            @PathVariable Long productId,
            @Valid @RequestBody CartAddReq req) {
        return R.ok(
                PromotionResConverter.add(
                cartAddCmdExe.execute(
                PromotionReqConverter.add(
                SecurityUtils.getUserId(), productId, req))));
    }
}
