package com.lemnt.product.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 当前SKU目录信息，仅供新报价和活动配置校验，不作为历史订单数据来源。
 */
public record ProductSkuBriefDTO(
    Long id,
    Long spuId,
    String code,
    String name,
    BigDecimal price,
    Integer stock,
    String image,
    List<ProductSnapshotDTO.Spec> specs,
    String status,
    BigDecimal weight,
    BigDecimal volume)
    implements Serializable {
}
