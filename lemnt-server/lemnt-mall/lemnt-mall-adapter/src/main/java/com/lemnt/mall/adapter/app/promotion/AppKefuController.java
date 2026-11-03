package com.lemnt.mall.adapter.app.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.mall.adapter.app.promotion.converter.AppKefuConverter;
import com.lemnt.mall.adapter.app.promotion.req.AppKefuMessageSendReq;
import com.lemnt.mall.adapter.app.promotion.res.AppKefuConversationRes;
import com.lemnt.mall.adapter.app.promotion.res.AppKefuMessageRes;
import com.lemnt.mall.application.promotion.executor.AppKefuConversationQueryExe;
import com.lemnt.mall.application.promotion.executor.AppKefuMessageListQueryExe;
import com.lemnt.mall.application.promotion.executor.AppKefuMessageSendCmdExe;
import com.lemnt.mall.application.promotion.executor.AppKefuReadCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/** 小程序客服会话与消息。 */
@Tag(name = "小程序客服")
@RestController
@RequestMapping("/mall/app/kefu")
@RequiredArgsConstructor
public class AppKefuController {
    private final AppKefuConversationQueryExe conversation;
    private final AppKefuMessageListQueryExe messages;
    private final AppKefuMessageSendCmdExe send;
    private final AppKefuReadCmdExe read;

    @GetMapping("/conversation")
    @Operation(summary = "本人客服会话")
    public R<AppKefuConversationRes> conversation() {
        return R.ok(AppKefuConverter.conversation(conversation.execute(SecurityUtils.getUserId())));
    }

    @GetMapping("/messages")
    @Operation(summary = "本人客服消息")
    public R<List<AppKefuMessageRes>> messages(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                    LocalDateTime beforeTime,
            @RequestParam(required = false) Integer limit) {
        return R.ok(
                messages
                        .execute(
                                AppKefuConverter.messages(
                                        SecurityUtils.getUserId(), beforeTime, limit))
                        .stream()
                        .map(AppKefuConverter::message)
                        .toList());
    }

    @PostMapping("/messages")
    @Operation(summary = "发送客服消息")
    public R<Long> send(@Valid @RequestBody AppKefuMessageSendReq req) {
        return R.ok(send.execute(AppKefuConverter.send(SecurityUtils.getUserId(), req)));
    }

    @PutMapping("/read")
    @Operation(summary = "标记本人客服消息已读")
    public R<Boolean> read() {
        return R.ok(read.execute(SecurityUtils.getUserId()));
    }
}
