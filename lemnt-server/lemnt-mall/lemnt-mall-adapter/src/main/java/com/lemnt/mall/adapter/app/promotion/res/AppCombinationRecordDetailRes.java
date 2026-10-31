package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** App 拼团详情。 */
@Schema(description = "App 拼团详情")
public record AppCombinationRecordDetailRes(
        @Schema(description = "团长记录")
        AppCombinationRecordRes headRecord,
        @Schema(description = "成员记录")
        List<AppCombinationRecordRes> memberRecords,
        @Schema(description = "当前用户订单编号")
        Long orderId
) implements java.io.Serializable {
}
