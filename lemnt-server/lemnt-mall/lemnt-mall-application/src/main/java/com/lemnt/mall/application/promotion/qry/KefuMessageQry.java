package com.lemnt.mall.application.promotion.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 客服消息查询，beforeTime 之前的最近 limit 条。 */
public record KefuMessageQry(Long conversationId, LocalDateTime beforeTime, Integer limit)
        implements Serializable {
}
