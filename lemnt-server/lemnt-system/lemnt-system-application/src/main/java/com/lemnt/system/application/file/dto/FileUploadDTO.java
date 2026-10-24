package com.lemnt.system.application.file.dto;

import java.io.Serializable;

/** 文件上传结果。 */
public record FileUploadDTO(String url) implements Serializable {
}
