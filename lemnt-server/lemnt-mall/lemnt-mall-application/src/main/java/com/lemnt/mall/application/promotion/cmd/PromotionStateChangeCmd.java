package com.lemnt.mall.application.promotion.cmd;

import com.lemnt.mall.domain.promotion.model.PromotionRoot.State;

/**
 * 营销状态修改命令。
 */
public record PromotionStateChangeCmd(
        long operatorId,
        String requestNo,
        long id,
        int expectedRevision,
        State target
) implements java.io.Serializable {}
