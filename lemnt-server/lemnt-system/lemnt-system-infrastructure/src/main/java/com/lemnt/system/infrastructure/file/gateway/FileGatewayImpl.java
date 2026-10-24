package com.lemnt.system.infrastructure.file.gateway;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.oss.service.OssFileService;
import com.lemnt.system.domain.file.gateway.FileGateway;
import com.lemnt.system.domain.file.model.UploadFile;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/** 复用公共 OSS 的类型、大小校验与 MinIO 存储。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileGatewayImpl implements FileGateway {
    private final OssFileService ossFileService;

    @Override
    public String upload(UploadFile file) {
        try {
            return ossFileService.uploadStream(file.content(), file.fileName(), file.contentType(), file.size());
        } catch (IOException e) {
            log.error("OSS 文件上传失败", e);
            throw new ServiceException("文件上传失败，请检查文件类型、大小或存储服务");
        }
    }
}
