package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 规格字典详情响应。 */
@Schema(description = "规格字典详情响应")
public record ProductSpecValueDetailRes(
        @Schema(description = "规格值ID")
        Long id,
        @Schema(description = "所属规格项ID")
        Long specId,
        @Schema(description = "规格值名称")
        String name,
        @Schema(description = "说明")
        String remark,
        @Schema(description = "创建时间；历史缺失不补造")
        LocalDateTime createTime
) implements Serializable {
}
