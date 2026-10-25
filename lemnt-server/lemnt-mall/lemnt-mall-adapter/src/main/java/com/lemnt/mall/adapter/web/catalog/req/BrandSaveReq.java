package com.lemnt.mall.adapter.web.catalog.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Schema(description = "BrandSaveReq")
public record BrandSaveReq(
        @Schema(description = "品牌名称") @NotBlank @Size(max = 100) String name,
        @Schema(description = "品牌图片URL") @Size(max = 500) String picUrl,
        @Schema(description = "品牌描述") @Size(max = 2000) String description,
        @Schema(description = "显示排序") @NotNull @Min(0) Integer sort,
        @Schema(description = "ENABLED或DISABLED") @NotBlank @Pattern(regexp = "ENABLED|DISABLED")
                String status)
        implements Serializable {}
