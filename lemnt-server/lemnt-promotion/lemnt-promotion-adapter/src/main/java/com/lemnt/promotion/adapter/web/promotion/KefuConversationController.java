package com.lemnt.promotion.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.promotion.adapter.web.promotion.converter.KefuConverter;
import com.lemnt.promotion.adapter.web.promotion.req.KefuConversationPinReq;
import com.lemnt.promotion.adapter.web.promotion.req.KefuMessageSendReq;
import com.lemnt.promotion.adapter.web.promotion.res.KefuConversationRes;
import com.lemnt.promotion.adapter.web.promotion.res.KefuMessageRes;
import com.lemnt.promotion.application.promotion.cmd.KefuConversationPinCmd;
import com.lemnt.promotion.application.promotion.executor.KefuConversationDeleteCmdExe;
import com.lemnt.promotion.application.promotion.executor.KefuConversationDetailQueryExe;
import com.lemnt.promotion.application.promotion.executor.KefuConversationListQueryExe;
import com.lemnt.promotion.application.promotion.executor.KefuConversationPinCmdExe;
import com.lemnt.promotion.application.promotion.executor.KefuMessageListQueryExe;
import com.lemnt.promotion.application.promotion.executor.KefuMessageSendCmdExe;
import com.lemnt.promotion.application.promotion.executor.KefuReadCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

/** 管理端客服会话与回复。 */
@Tag(name = "客服管理")
@RestController
@RequestMapping("/mall/promotion/kefu-conversations")
@RequiredArgsConstructor
public class KefuConversationController {
    private final KefuConversationListQueryExe list;
    private final KefuConversationDetailQueryExe detail;
    private final KefuMessageListQueryExe messages;
    private final KefuMessageSendCmdExe send;
    private final KefuReadCmdExe read;
    private final KefuConversationPinCmdExe pin;
    private final KefuConversationDeleteCmdExe delete;

    @GetMapping
    @Operation(summary = "客服会话列表")
    @RequiresPermissions("mall:promotion:list")
    public R<List<KefuConversationRes>> list() {
        return R.ok(list.execute().stream().map(KefuConverter::conversation).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "客服会话详情")
    @RequiresPermissions("mall:promotion:query")
    public R<KefuConversationRes> detail(@PathVariable Long id) {
        return R.ok(KefuConverter.conversation(detail.execute(id)));
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "客服会话消息")
    @RequiresPermissions("mall:promotion:query")
    public R<List<KefuMessageRes>> messages(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                    LocalDateTime beforeTime,
            @RequestParam(required = false) Integer limit) {
        return R.ok(
                messages.execute(KefuConverter.messages(id, beforeTime, limit)).stream()
                        .map(KefuConverter::message)
                        .toList());
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "管理员回复客服消息")
    @RequiresPermissions("mall:promotion:create")
    public R<Long> send(@PathVariable Long id, @Valid @RequestBody KefuMessageSendReq req) {
        return R.ok(send.execute(KefuConverter.send(id, SecurityUtils.getUserId(), req)));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "管理员标记会话已读")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> read(@PathVariable Long id) {
        return R.ok(read.execute(id));
    }

    @PutMapping("/{id}/pinned")
    @Operation(summary = "置顶或取消置顶会话")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> pinned(
            @PathVariable Long id, @Valid @RequestBody KefuConversationPinReq req) {
        return R.ok(pin.execute(new KefuConversationPinCmd(id, req.pinned())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除客服会话")
    @RequiresPermissions("mall:promotion:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }
}
