package com.lemnt.common.oss.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.oss.configure.MinioConfig;
import com.lemnt.common.oss.utils.ImageUtilsV2;
import io.minio.*;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Oss 文件上传服务
 *
 * @author wangxx
 **/
@Component
public class OssFileService {

    private static final Logger logger = LoggerFactory.getLogger(OssFileService.class);
    private static final long MEMORY_THRESHOLD = 20 * 1024 * 1024;

    private final MinioConfig minioConfig;
    private final MinioClient minioClient;
    private final OssFileValidator fileValidator;

    @Value("${upload.video.max-size}")
    private Integer videoMaxSize = 100;

    @Value("${upload.image.max-size}")
    private Integer imageMaxSize = 2;

    public OssFileService(MinioConfig minioConfig, MinioClient minioClient, OssFileValidator fileValidator) {
        this.minioConfig = minioConfig;
        this.minioClient = minioClient;
        this.fileValidator = fileValidator;
    }

    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String originalFileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        long size = file.getSize();
        fileValidator.validateFile(originalFileName, contentType, size, imageMaxSize, videoMaxSize);
        try (InputStream is = file.getInputStream()) {
            return uploadStream(is, originalFileName, contentType, size);
        }
    }

    /**
     * 上传流文件
     */
    public String uploadStream(InputStream inputStream, String fileName, String contentType, long fileSize) throws IOException {
        try {
            logger.info("Uploading file: {}", fileName);
            fileValidator.validateFile(fileName, contentType, fileSize, imageMaxSize, videoMaxSize);
            if (fileSize <= MEMORY_THRESHOLD) {
                byte[] bytes = IoUtil.readBytes(inputStream);
                return uploadSmallFile(bytes, fileName, contentType);
            } else {
                return uploadLargeFile(inputStream, fileName, contentType);
            }
        } catch (MinioException e) {
            logger.error("Minio upload failed: {}", e.getMessage());
            throw new IOException("Minio Failed to upload file", e);
        } catch (Exception e) {
            logger.error("Unexpected error during file upload: {}", e.getMessage());
            throw new IOException("Unexpected error during file upload", e);
        }
    }

    private String uploadSmallFile(byte[] bytes, String fileName, String contentType)
            throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        String md5 = DigestUtils.md5DigestAsHex(bytes);
        String objectName = buildObjectName(md5, fileName);
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(objectName)
                .stream(new ByteArrayInputStream(bytes), (long) bytes.length, -1L)
                .contentType(StrUtil.isNotBlank(contentType) ? contentType : "application/octet-stream")
                .build();
        minioClient.putObject(args);
        logger.info("小文件 uploaded successfully: {}", objectName);
        return buildUrl(objectName);
    }

    private String uploadLargeFile(InputStream inputStream, String fileName, String contentType)
            throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        File tempFile = File.createTempFile("minio-upload-", ".tmp");
        try {
            FileUtil.writeFromStream(inputStream, tempFile);
            try (InputStream in = FileUtil.getInputStream(tempFile)) {
                String md5 = DigestUtils.md5DigestAsHex(in);
                String objectName = buildObjectName(md5, fileName);

                if (exists(objectName)) {
                    logger.info("File already exists, returning cached URL: {}", objectName);
                    return buildUrl(objectName);
                }

                try (InputStream in2 = FileUtil.getInputStream(tempFile)) {
                    PutObjectArgs args = PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(in2, tempFile.length(), -1L)
                            .contentType(StrUtil.isNotBlank(contentType) ? contentType : "application/octet-stream")
                            .build();
                    minioClient.putObject(args);
                }
                logger.info("大文件 uploaded successfully: {}", objectName);
                return buildUrl(objectName);
            }
        } finally {
            FileUtil.del(tempFile);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        try {
            String minioFile = StringUtils.substringAfter(fileUrl, minioConfig.getBucketName());
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(minioFile)
                    .build());
            logger.info("File deleted successfully: {}", fileUrl);
        } catch (MinioException e) {
            logger.error("Minio file delete failed: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during file delete: {}", e.getMessage());
        }
    }

    /**
     * 获取文件流
     */
    public InputStream getFile(String fileUrl) {
        try {
            String bucketName = minioConfig.getBucketName();
            String objectName = StringUtils.substringAfter(fileUrl, bucketName + "/");
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            logger.error("MinIO 获取文件失败: {}", fileUrl, e);
            throw new RuntimeException("MinIO 获取文件失败", e);
        }
    }

    private boolean exists(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String buildObjectName(String md5, String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return md5 + fileName.substring(fileName.lastIndexOf("."));
        }
        return md5;
    }

    private String buildUrl(String objectName) {
        return minioConfig.getUrl() + "/" + minioConfig.getBucketName() + "/" + objectName;
    }

    public String getResizeUrl(String url, int width, int height) throws Exception {
        byte[] bytes = ImageUtilsV2.getInputStream(url);
        String suffix = ImageUtilsV2.getSuffix(url);
        byte[] fileBytes = ImageUtilsV2.resizeAndSaveImage(
                new ByteArrayInputStream(bytes),
                width, height
        );
        return this.uploadStream(new ByteArrayInputStream(fileBytes), RandomUtil.randomString(18) + "." + suffix, "image/" + suffix, fileBytes.length);
    }
}
