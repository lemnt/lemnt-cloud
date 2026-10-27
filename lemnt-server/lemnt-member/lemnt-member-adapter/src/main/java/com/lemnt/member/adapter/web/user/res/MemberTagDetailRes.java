package com.lemnt.member.adapter.web.user.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

/**
 * 标签响应。
 */
@Schema(description = "会员Tag响应")
public record MemberTagDetailRes(
        @Schema(description = "记录编号")
        Long id,
        @Schema(description = "名称或收件人")
        String name,
        @Schema(description = "备注")
        String remark
) implements Serializable {}
