package com.lemnt.system.adapter.web.file.converter;

import com.lemnt.system.adapter.web.file.res.FileUploadRes;
import com.lemnt.system.application.file.dto.FileUploadDTO;

/** 上传结果边界转换。 */
public class FileResConverter {
    public static FileUploadRes toRes(FileUploadDTO dto) {
        return new FileUploadRes(dto.url());
    }
}
