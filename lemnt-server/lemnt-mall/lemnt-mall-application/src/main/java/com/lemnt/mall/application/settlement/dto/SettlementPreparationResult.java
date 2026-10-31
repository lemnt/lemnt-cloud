package com.lemnt.mall.application.settlement.dto;

import com.lemnt.mall.domain.settlement.model.SettlementOrder;

/** 结算准备结果：订单快照及是否本次新建。 */
public record SettlementPreparationResult(SettlementOrder order, boolean created) {}
