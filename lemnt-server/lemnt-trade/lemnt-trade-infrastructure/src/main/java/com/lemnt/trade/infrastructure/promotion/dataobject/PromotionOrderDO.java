package com.lemnt.trade.infrastructure.promotion.dataobject;

import lombok.Data;

@Data
public class PromotionOrderDO {
    private String orderNo;
    private Long memberId;
    private String status;
}
