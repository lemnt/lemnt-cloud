package com.lemnt.system.adapter.app.file.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

/** 会员图片上传请求。 */
@Schema(description = "会员图片上传请求")
public record ImageUploadReq(
        @Schema(description = "图片文件") @NotNull org.springframework.web.multipart.MultipartFile file)
        implements java.io.Serializable {}
