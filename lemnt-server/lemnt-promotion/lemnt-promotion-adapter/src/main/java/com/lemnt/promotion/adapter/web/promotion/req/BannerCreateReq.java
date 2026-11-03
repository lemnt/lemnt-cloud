package com.lemnt.promotion.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Banner 新增请求。 */
@Schema(description = "Banner 新增请求")
public record BannerCreateReq(
        @Schema(description = "标题") @NotBlank @Size(max = 100) String title,
        @Schema(description = "跳转链接") @NotBlank @Size(max = 500) String url,
        @Schema(description = "图片地址") @NotBlank @Size(max = 500) String picUrl,
        @Schema(description = "排序") @NotNull Integer sort,
        @Schema(description = "0 开启，1 关闭") @NotNull @Min(0) @Max(1) Integer status,
        @Schema(description = "位置：1首页、2秒杀、3拼团/砍价、4折扣、5满减") @NotNull @Min(1) @Max(5)
                Integer position,
        @Schema(description = "备注") @Size(max = 255) String memo)
        implements java.io.Serializable {}
