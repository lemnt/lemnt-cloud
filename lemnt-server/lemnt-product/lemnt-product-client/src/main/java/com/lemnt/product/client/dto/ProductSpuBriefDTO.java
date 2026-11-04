package com.lemnt.product.client.dto;

import java.io.Serializable;

/**
 * 营销所需当前商品主体信息，历史订单不得使用此结果覆盖快照。
 */
public record ProductSpuBriefDTO(Long id, String name, Long categoryId, String image, String status)
    implements Serializable {
}
