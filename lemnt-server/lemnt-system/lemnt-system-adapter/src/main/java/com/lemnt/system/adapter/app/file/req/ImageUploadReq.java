package com.lemnt.system.adapter.app.file.req;
/** 会员图片上传请求。 */
public record ImageUploadReq(@jakarta.validation.constraints.NotNull org.springframework.web.multipart.MultipartFile file) {}
