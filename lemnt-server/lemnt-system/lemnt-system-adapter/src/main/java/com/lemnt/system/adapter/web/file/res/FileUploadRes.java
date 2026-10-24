package com.lemnt.system.adapter.web.file.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 与前端 data.url 契约一致的上传结果。 */
@Schema(description = "统一文件上传结果")
public record FileUploadRes(
        @Schema(description = "OSS 文件访问地址")
        String url
) implements Serializable {
}
