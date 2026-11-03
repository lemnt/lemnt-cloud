package com.lemnt.promotion.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/** 客服消息持久化对象。 */
@Data
@TableName("promotion_kefu_message")
public class KefuMessageDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long conversationId;
    private Long senderId;
    private Integer senderType;
    private Long receiverId;
    private Integer receiverType;
    private Integer contentType;
    private String content;
    private Boolean readStatus;
    private LocalDateTime createTime;
}
