package com.lemnt.member.adapter.web.user.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

/**
 * 分组响应。
 */
@Schema(description = "会员Group响应")
public record MemberGroupSimpleRes(
        @Schema(description = "记录编号")
        Long id,
        @Schema(description = "名称或收件人")
        String name,
        @Schema(description = "备注")
        String remark,
        @Schema(description = "状态：1启用，0停用")
        Integer status
) implements Serializable {}
