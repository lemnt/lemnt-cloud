package com.lemnt.system.adapter.web.file.req;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import jakarta.validation.constraints.NotNull;

/** multipart 上传请求。 */
@Schema(description = "统一文件上传请求")
public record FileUploadReq(
        @Schema(description = "上传文件，multipart 字段名为 file")
        @NotNull(message = "上传文件不能为空")
        MultipartFile file
) implements Serializable {
}
