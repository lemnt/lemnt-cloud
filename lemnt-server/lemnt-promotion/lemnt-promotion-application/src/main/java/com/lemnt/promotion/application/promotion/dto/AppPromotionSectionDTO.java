package com.lemnt.promotion.application.promotion.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 活动聚合分区。
 */
public record AppPromotionSectionDTO(String type, String label, List<AppPromotionItemDTO> items)
    implements Serializable {
    public AppPromotionSectionDTO {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
