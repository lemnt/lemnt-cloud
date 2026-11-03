package com.lemnt.mall.application.settlement.qry;

import java.io.Serializable;

/** SettlementRequestDetailQry 输入契约。 */
public record SettlementRequestDetailQry(Long memberId, String requestNo) implements Serializable {}
