package com.lemnt.member.adapter.app.address.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

/**
 * 地址响应。
 */
@Schema(description = "会员Address响应")
public record AppMemberAddressDetailRes(
        @Schema(description = "记录编号")
        Long id,
        @Schema(description = "会员编号")
        Long memberId,
        @Schema(description = "名称或收件人")
        String name,
        @Schema(description = "手机号")
        String mobile,
        @Schema(description = "地区编号")
        Long areaId,
        @Schema(description = "详细地址")
        String detailAddress,
        @Schema(description = "是否默认地址")
        Boolean defaultStatus
) implements Serializable {}
