package com.lemnt.member.adapter.web.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 查询标签分配请求。
 */
@Schema(description = "TagsAssign请求")
public record TagsAssignReq(
        @Schema(description = "标签编号，空集合清空标签")
        @NotNull
        @Size(max = 100)
        List<Long> tagIds
) implements Serializable {}
