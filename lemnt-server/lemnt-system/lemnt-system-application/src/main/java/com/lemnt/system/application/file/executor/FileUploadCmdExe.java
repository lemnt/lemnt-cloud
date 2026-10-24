package com.lemnt.system.application.file.executor;

import com.lemnt.system.application.file.cmd.FileUploadCmd;
import com.lemnt.system.application.file.dto.FileUploadDTO;
import com.lemnt.system.domain.file.gateway.FileGateway;
import com.lemnt.system.domain.file.model.UploadFile;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 编排统一上传，不感知 OSS 实现。 */
@Component
@RequiredArgsConstructor
public class FileUploadCmdExe {
    private final FileGateway fileGateway;

    /** 将上传命令交由存储网关处理。 */
    public FileUploadDTO execute(FileUploadCmd cmd) {
        return new FileUploadDTO(fileGateway.upload(
                new UploadFile(cmd.content(), cmd.fileName(), cmd.contentType(), cmd.size())));
    }
}
