package com.lemnt.system.domain.file.model;

import java.io.InputStream;

/** 待上传文件，只传递文件流与元数据，不依赖 HTTP 类型。 */
public record UploadFile(InputStream content, String fileName, String contentType, long size) {
}
