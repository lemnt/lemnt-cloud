package com.lemnt.order.adapter.web.pickup.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 自提门店新增/修改请求。 */
@Schema(description = "自提门店新增/修改请求")
public record PickUpStoreSaveReq(
        @Schema(description = "门店名称") @NotBlank @Size(max = 100) String name,
        @Schema(description = "门店简介") @Size(max = 500) String introduction,
        @Schema(description = "门店电话") @NotBlank @Size(max = 32) String phone,
        @Schema(description = "地区编号") @NotNull Long areaId,
        @Schema(description = "详细地址") @NotBlank @Size(max = 255) String detailAddress,
        @Schema(description = "门店 logo") @Size(max = 500) String logo,
        @Schema(description = "开门时间 HH:mm:ss") @NotBlank @Size(max = 8) String openingTime,
        @Schema(description = "关门时间 HH:mm:ss") @NotBlank @Size(max = 8) String closingTime,
        @Schema(description = "纬度") BigDecimal latitude,
        @Schema(description = "经度") BigDecimal longitude,
        @Schema(description = "可核销管理员编号") List<Long> verifyUserIds,
        @Schema(description = "0 开启、1 关闭") @NotNull Integer status)
        implements Serializable {}
