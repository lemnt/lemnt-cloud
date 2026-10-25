package com.lemnt.mall.infrastructure.catalog.dataobject;

import lombok.Data;

@Data
public class StockAdjustmentDO {
    private String requestNo;
    private Long productId;
    private Integer delta;
    private String reason;
    private Integer resultingStock;
}
