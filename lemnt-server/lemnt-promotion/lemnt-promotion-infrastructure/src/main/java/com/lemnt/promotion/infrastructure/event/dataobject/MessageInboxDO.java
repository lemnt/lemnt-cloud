package com.lemnt.promotion.infrastructure.event.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 已成功消费的本地事务凭证，不能在业务事务之前独立提交。
 */
@Data
@Accessors(chain = true)
@TableName("promotion_message_inbox")
public class MessageInboxDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String consumerKey;
    private String eventNo;
    private String eventType;
    private String businessKey;
    private String payloadHash;
    private LocalDateTime createTime;
}
