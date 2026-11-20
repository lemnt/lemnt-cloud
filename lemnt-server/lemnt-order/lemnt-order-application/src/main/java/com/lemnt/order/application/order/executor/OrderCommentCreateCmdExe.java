package com.lemnt.order.application.order.executor;

import com.lemnt.order.application.order.cmd.OrderCommentCreateCmd;
import com.lemnt.order.application.order.service.OrderCommentService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 会员评价投稿执行器。
 */
@Component
@RequiredArgsConstructor
public class OrderCommentCreateCmdExe {
    private final OrderCommentService commentService;

    /**
     * 执行评价投稿。
     */
    public Long execute(OrderCommentCreateCmd cmd) {
        return commentService.create(cmd);
    }
}
