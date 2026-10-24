package com.lemnt.system.adapter.web.config.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典数据分页查询条件对象
 *
 * @author wangxx
 */
@Schema(description = "字典数据分页查询条件")
public record DictDataPageReq(
        /**
         * 字典类型
         */
        @Schema(description = "字典类型")
        String dictType,
        /**
         * 字典标签
         */
        @Schema(description = "字典标签")
        String dictLabel,
        /**
         * 状态（1正常 2停用）
         */
        @Schema(description = "状态（1正常 2停用）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status,
        @Schema(description = "页码")
        Integer pageNum,
        @Schema(description = "每页数量")
        Integer pageSize,
        @Schema(description = "开始时间")
        LocalDateTime beginTime,
        @Schema(description = "结束时间")
        LocalDateTime endTime
) implements Serializable {

    public DictDataPageReq {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
    }
}
