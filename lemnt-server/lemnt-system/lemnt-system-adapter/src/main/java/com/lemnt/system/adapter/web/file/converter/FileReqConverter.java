package com.lemnt.system.adapter.web.file.converter;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.system.adapter.web.file.req.FileUploadReq;
import com.lemnt.system.application.file.cmd.FileUploadCmd;

import java.io.IOException;

/** multipart 请求到上传命令的边界转换。 */
public class FileReqConverter {
    public static FileUploadCmd toCmd(FileUploadReq req) throws IOException {
        var file = req.file();
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        return new FileUploadCmd(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), file.getSize());
    }
}
