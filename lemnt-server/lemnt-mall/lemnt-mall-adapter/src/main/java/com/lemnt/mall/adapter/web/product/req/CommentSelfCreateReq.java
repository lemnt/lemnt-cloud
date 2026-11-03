package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import java.util.List;

/** 后台自评请求。 */
@Schema(description = "后台商品自评请求")
public record CommentSelfCreateReq(
        @Schema(description = "评价用户昵称") @NotBlank @Size(max = 80) String userNickname,
        @Schema(description = "评价用户头像地址")
                @NotBlank
                @Size(max = 1024)
                @Pattern(regexp = "https://[^\\s]+")
                String userAvatar,
        @Schema(description = "商品SKU编号") @NotNull @Positive Long skuId,
        @Schema(description = "描述相符评分，1至5分") @NotNull @Min(1) @Max(5) Integer descriptionScores,
        @Schema(description = "服务评分，1至5分") @NotNull @Min(1) @Max(5) Integer benefitScores,
        @Schema(description = "评价内容") @NotBlank @Size(max = 1000) String content,
        @Schema(description = "评价图片地址，最多9张") @Size(max = 9)
                List<@NotBlank @Size(max = 1024) @Pattern(regexp = "https://[^\\s]+") String>
                        picUrls)
        implements java.io.Serializable {}
