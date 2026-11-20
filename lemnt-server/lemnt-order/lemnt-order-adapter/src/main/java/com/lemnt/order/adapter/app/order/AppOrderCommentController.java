package com.lemnt.order.adapter.app.order;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.order.adapter.app.order.req.AppOrderCommentCreateReq;
import com.lemnt.order.adapter.app.order.res.AppOrderCommentCreateRes;
import com.lemnt.order.application.order.cmd.OrderCommentCreateCmd;
import com.lemnt.order.application.order.executor.OrderCommentCreateCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

/**
 * 会员评价投稿入口；购买核验在交易域完成，商品服务只按已验证事实落库。
 */
@Tag(name = "会员订单评价")
@RestController
@RequestMapping("/mall/app/comments")
@RequiredArgsConstructor
public class AppOrderCommentController {
    private final OrderCommentCreateCmdExe commentCreateCmdExe;

    @PostMapping
    @Operation(summary = "评价本人已完成订单内商品")
    public R<AppOrderCommentCreateRes> create(@Valid @RequestBody AppOrderCommentCreateReq req) {
        var id =
            commentCreateCmdExe.execute(
                new OrderCommentCreateCmd(
                    SecurityUtils.getUserId(),
                    req.orderNo(),
                    req.skuId(),
                    req.orderItemId(),
                    req.score(),
                    req.content(),
                    req.descriptionScores(),
                    req.benefitScores(),
                    req.anonymous(),
                    req.picUrls(),
                    SecurityUtils.getNickName(),
                    SecurityUtils.getUserAvatar()));
        return R.ok(new AppOrderCommentCreateRes(id));
    }
}
