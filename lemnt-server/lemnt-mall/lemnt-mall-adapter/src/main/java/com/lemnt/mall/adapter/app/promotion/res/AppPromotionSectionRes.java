package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/** 小程序活动聚合分区。 */
@Schema(description = "小程序活动聚合分区")
public record AppPromotionSectionRes(
        @Schema(description = "类型") String type,
        @Schema(description = "名称") String label,
        @Schema(description = "活动列表") List<Item> items
) implements java.io.Serializable {
    public AppPromotionSectionRes {
        items = items == null ? List.of() : List.copyOf(items);
    }

    /** 聚合条目。 */
    @Schema(description = "活动聚合条目")
    public record Item(
            @Schema(description = "活动编号") Long id,
            @Schema(description = "活动名称") String name,
            @Schema(description = "开始时间") LocalDateTime startTime,
            @Schema(description = "结束时间") LocalDateTime endTime
    ) implements java.io.Serializable {
    }
}
