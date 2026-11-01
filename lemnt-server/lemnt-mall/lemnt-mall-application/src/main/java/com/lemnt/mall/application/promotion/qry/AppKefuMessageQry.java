package com.lemnt.mall.application.promotion.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 小程序本人客服消息查询。 */
public record AppKefuMessageQry(Long memberId, LocalDateTime beforeTime, Integer limit)
        implements Serializable {
}
