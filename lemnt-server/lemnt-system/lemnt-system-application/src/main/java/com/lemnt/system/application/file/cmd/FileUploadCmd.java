package com.lemnt.system.application.file.cmd;

import java.io.InputStream;

/** 文件上传命令，文件流由 Adapter 管理关闭。 */
public record FileUploadCmd(InputStream content,
        String fileName,
        String contentType,
        long size) implements java.io.Serializable {
}
