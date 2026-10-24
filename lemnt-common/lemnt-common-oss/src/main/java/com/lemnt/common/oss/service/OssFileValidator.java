package com.lemnt.common.oss.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 文件上传校验器
 *
 * @author wangxx
 */
@Component
public class OssFileValidator {

    private static final List<String> VIDEO_TYPES = Arrays.asList("mp4", "avi", "mov", "webm");
    private static final List<String> IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png");
    private static final List<String> SHEET_TYPES = Arrays.asList("xls", "xlsx", "csv");
    private static final long MAX_SHEET_SIZE = 5;

    /**
     * 校验文件大小和类型
     *
     * @param fileName     文件名
     * @param contentType  MIME 类型
     * @param fileSize     文件大小
     * @param imageMaxSize 图片最大大小（MB）
     * @param videoMaxSize 视频最大大小（MB）
     * @throws IllegalArgumentException 文件不符合要求时抛出
     */
    public void validateFile(String fileName, String contentType, long fileSize,
                             Integer imageMaxSize, Integer videoMaxSize) {
        if (fileSize <= 0) {
            throw new IllegalArgumentException("无效文件");
        }

        String ext = null;
        if (fileName != null && fileName.contains(".")) {
            ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        if (ext == null) {
            throw new IllegalArgumentException("无法识别文件类型");
        }

        if (IMAGE_TYPES.contains(ext)) {
            if (fileSize > ((long) imageMaxSize * 1024 * 1024)) {
                throw new IllegalArgumentException("图片大小不能超过 " + imageMaxSize + " MB");
            }
            if (contentType != null && !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("图片文件类型和内容不匹配");
            }
        } else if (VIDEO_TYPES.contains(ext)) {
            if (fileSize > ((long) videoMaxSize * 1024 * 1024)) {
                throw new IllegalArgumentException("视频大小不能超过 " + videoMaxSize + " MB");
            }
            if (contentType != null && !contentType.startsWith("video/")) {
                throw new IllegalArgumentException("视频文件类型和内容不匹配");
            }
        } else if (SHEET_TYPES.contains(ext)) {
            if (fileSize > (MAX_SHEET_SIZE * 1024 * 1024)) {
                throw new IllegalArgumentException("表格大小不能超过 " + MAX_SHEET_SIZE + " MB");
            }
            if (contentType != null && !(contentType.contains("spreadsheet") || contentType.contains("csv"))) {
                throw new IllegalArgumentException("表格文件类型和内容不匹配");
            }
        } else {
            throw new IllegalArgumentException("不支持的文件类型，仅支持图片/视频/表格");
        }
    }
}
