package com.lemnt.order.infrastructure.event.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/** 本库事务消息记录，载荷为已冻结的完整信封。 */
@Data
@Accessors(chain = true)
@TableName("order_message_outbox")
public class MessageOutboxDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String eventNo;
    private String eventType;
    private String businessKey;
    private String aggregateNo;
    private Integer schemaVersion;
    private String payload;
    private String payloadHash;
    private String status;
    private Integer attempts;
    private Long executeVersion;
    private LocalDateTime nextRetryAt;
    private LocalDateTime leaseUntil;
    private String lastError;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
