package com.lemnt.trade.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lemnt.common.infrastructure.domain.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("trade_outbox")
public class TradeOutboxDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String eventId;
    private String aggregateType;
    private String aggregateNo;
    private String eventType;
    private String payload;
    private String status;
    private Integer retryCount;
    private String leaseToken;
    private java.time.LocalDateTime leaseUntil;
    private java.time.LocalDateTime nextAttemptAt;
}