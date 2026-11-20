package com.lemnt.promotion.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理端客服消息。
 */
@Schema(description = "管理端客服消息")
public record KefuMessageRes(
    @Schema(description = "消息编号") Long id,
    @Schema(description = "会话编号") Long conversationId,
    @Schema(description = "发送人编号") Long senderId,
    @Schema(description = "发送人类型：1会员、2管理员") Integer senderType,
    @Schema(description = "消息类型") Integer contentType,
    @Schema(description = "消息内容") String content,
    @Schema(description = "是否已读") Boolean readStatus,
    @Schema(description = "发送时间") LocalDateTime createTime)
    implements Serializable {
}
