package com.lemnt.member.adapter.web.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

/**
 * 分组分配请求。
 */
@Schema(description = "GroupAssign请求")
public record GroupAssignReq(
        @Schema(description = "分组编号，可空")
        Long groupId
) implements Serializable {}
