package com.lemnt.system.adapter.web.org.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位分页查询条件对象
 *
 * @author wangxx
 */
@Schema(description = "岗位分页查询条件")
public record PostPageReq(
        /**
         * 岗位编码
         */
        @Schema(description = "岗位编码")
        String postCode,
        /**
         * 岗位名称
         */
        @Schema(description = "岗位名称")
        String postName,
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

    public PostPageReq {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
    }
}
