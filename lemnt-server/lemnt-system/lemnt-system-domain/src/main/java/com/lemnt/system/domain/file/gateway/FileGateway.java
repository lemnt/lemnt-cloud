package com.lemnt.system.domain.file.gateway;

import com.lemnt.system.domain.file.model.UploadFile;

/** 统一文件存储契约。 */
public interface FileGateway {
    /** 上传文件并返回可访问地址。 */
    String upload(UploadFile file);
}
