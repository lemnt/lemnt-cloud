package com.lemnt.member.adapter.app.address.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 地址保存请求。
 */
@Schema(description = "AddressSave请求")
public record AppMemberAddressCreateReq(
        @Schema(description = "名称或收件人")
        @NotBlank
        @Size(max = 80)
        String name,
        @Schema(description = "手机号")
        @NotBlank
        @Size(max = 24)
        String mobile,
        @Schema(description = "地区编号")
        @NotNull
        @Positive
        Long areaId,
        @Schema(description = "详细地址")
        @NotBlank
        @Size(max = 500)
        String detailAddress,
        @Schema(description = "是否默认地址")
        @NotNull
        Boolean defaultStatus
) implements Serializable {}
