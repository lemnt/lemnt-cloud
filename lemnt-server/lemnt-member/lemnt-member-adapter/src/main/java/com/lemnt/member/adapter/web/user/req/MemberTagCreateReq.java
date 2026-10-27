package com.lemnt.member.adapter.web.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 标签保存请求。
 */
@Schema(description = "TagSave请求")
public record MemberTagCreateReq(
        @Schema(description = "名称或收件人")
        @NotBlank
        @Size(max = 80)
        String name,
        @Schema(description = "备注")
        @Size(max = 500)
        String remark
) implements Serializable {}
