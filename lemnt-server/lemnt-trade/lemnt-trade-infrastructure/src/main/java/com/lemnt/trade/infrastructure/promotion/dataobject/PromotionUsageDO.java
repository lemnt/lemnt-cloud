package com.lemnt.trade.infrastructure.promotion.dataobject;

import lombok.Data;

@Data
public class PromotionUsageDO {
    private String orderNo;
    private Long rootId;
    private Integer version;
    private Long memberId;
    private Long discountFen;
    private String ruleHash;
    private String state;
}
