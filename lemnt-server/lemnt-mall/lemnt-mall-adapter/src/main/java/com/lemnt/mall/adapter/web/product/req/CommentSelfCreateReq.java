package com.lemnt.mall.adapter.web.product.req;
import jakarta.validation.constraints.*;
import java.util.List;
/** 后台自评请求。 */
public record CommentSelfCreateReq(
 @NotBlank @Size(max=80) String userNickname,
 @NotBlank @Size(max=1024) @Pattern(regexp="https://[^\\s]+") String userAvatar,
 @NotNull @Positive Long skuId,
 @NotNull @Min(1) @Max(5) Integer descriptionScores,
 @NotNull @Min(1) @Max(5) Integer benefitScores,
 @NotBlank @Size(max=1000) String content,
 @Size(max=9) List<@NotBlank @Size(max=1024) @Pattern(regexp="https://[^\\s]+") String> picUrls
) {}
