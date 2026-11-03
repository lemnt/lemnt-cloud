package com.lemnt.system.adapter.web.config.req;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 字典数据分页HTTP参数，分页默认值与其他系统列表一致。 */
@Schema(description = "字典数据分页查询")
public record DictDataPageReq(
        @Schema(description = "字典类型") String dictType,
        @Schema(description = "字典标签") String dictLabel,
        @Schema(description = "状态：1正常，2停用") @Pattern(regexp = "[12]") String status,
        @Schema(description = "页码，从1开始") @Min(1) Integer pageNum,
        @Schema(description = "每页条数") @Min(1) Integer pageSize,
        @Schema(description = "开始时间，yyyy-MM-dd HH:mm:ss")
                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                LocalDateTime beginTime,
        @Schema(description = "结束时间，yyyy-MM-dd HH:mm:ss")
                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                LocalDateTime endTime)
        implements Serializable {
    public DictDataPageReq {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
    }
}
