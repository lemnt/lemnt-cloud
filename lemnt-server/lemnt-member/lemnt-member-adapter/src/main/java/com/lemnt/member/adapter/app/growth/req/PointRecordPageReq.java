package com.lemnt.member.adapter.app.growth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.Serializable;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

/** 本人积分记录分页请求。 */
@Schema(description = "本人积分记录分页请求")
public record PointRecordPageReq(
        @Schema(description = "true收入，false支出，未填全部")
        Boolean addStatus,
        @Schema(description = "创建时间起，包含边界")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
        @Schema(description = "创建时间止，包含边界")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
        @Schema(description = "页码，默认1")
        @Min(1) @Max(1000000)
        Integer pageNum,
        @Schema(description = "每页条数，默认20，最大100")
        @Min(1) @Max(100)
        Integer pageSize
) implements Serializable {}
