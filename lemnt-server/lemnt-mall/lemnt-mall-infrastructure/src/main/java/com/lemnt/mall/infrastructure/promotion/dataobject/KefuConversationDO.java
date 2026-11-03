package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/** 客服会话持久化对象。 */
@Data
@TableName("mall_promotion_kefu_conversation")
public class KefuConversationDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private LocalDateTime lastMessageTime;
    private String lastMessageContent;
    private Integer lastMessageContentType;
    private Boolean adminPinned;
    private Boolean adminDeleted;
    private Integer adminUnreadMessageCount;
}
