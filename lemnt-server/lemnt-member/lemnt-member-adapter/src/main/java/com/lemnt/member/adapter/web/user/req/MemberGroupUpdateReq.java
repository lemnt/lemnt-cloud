package com.lemnt.member.adapter.web.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 分组保存请求。
 */
@Schema(description = "GroupSave请求")
public record MemberGroupUpdateReq(
        @Schema(description = "名称或收件人")
        @NotBlank
        @Size(max = 80)
        String name,
        @Schema(description = "备注")
        @Size(max = 500)
        String remark,
        @Schema(description = "状态：1启用，0停用")
        @NotNull
        Integer status
) implements Serializable {}
