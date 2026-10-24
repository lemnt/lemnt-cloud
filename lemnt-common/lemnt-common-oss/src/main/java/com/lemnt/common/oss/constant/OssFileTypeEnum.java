package com.lemnt.common.oss.constant;


/**
 * Oss 文件类型
 *
 * @author wangxx
 */
public enum OssFileTypeEnum {
    XLSX("xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "Excel 2007+"),
    XLS("xls",
            "application/vnd.ms-excel",
            "Excel 97-2003"),
    CSV("csv",
            "text/csv",
            "CSV 文件"),

    /* ================= 文档 ================= */
    PDF("pdf",
            "application/pdf",
            "PDF 文件"),
    DOCX("docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "Word 2007+"),
    DOC("doc",
            "application/msword",
            "Word 97-2003"),

    TXT("txt",
            "text/plain",
            "文本文件"),

    /* ================= 图片 ================= */
    JPG("jpg",
            "image/jpeg",
            "JPEG 图片"),
    JPEG("jpeg",
            "image/jpeg",
            "JPEG 图片"),
    PNG("png",
            "image/png",
            "PNG 图片"),
    GIF("gif",
            "image/gif",
            "GIF 图片"),
    BMP("bmp",
            "image/bmp",
            "BMP 图片"),
    WEBP("webp",
            "image/webp",
            "WEBP 图片"),

    /* ================= 音频 ================= */
    MP3("mp3",
            "audio/mpeg",
            "MP3 音频"),
    WAV("wav",
            "audio/wav",
            "WAV 音频"),
    AAC("aac",
            "audio/aac",
            "AAC 音频"),

    /* ================= 视频 ================= */
    MP4("mp4",
            "video/mp4",
            "MP4 视频"),
    AVI("avi",
            "video/x-msvideo",
            "AVI 视频"),
    MOV("mov",
            "video/quicktime",
            "MOV 视频"),

    /* ================= 压缩包 ================= */
    ZIP("zip",
            "application/zip",
            "ZIP 压缩包"),
    RAR("rar",
            "application/vnd.rar",
            "RAR 压缩包"),
    TAR("tar",
            "application/x-tar",
            "TAR 压缩包"),
    GZ("gz",
            "application/gzip",
            "GZIP 压缩包"),

    /* ================= 其他 ================= */
    JSON("json",
            "application/json",
            "JSON 文件"),
    XML("xml",
            "application/xml",
            "XML 文件"),
    OCTET_STREAM("bin",
            "application/octet-stream",
            "二进制文件"),
    ;

    private final String suffix;
    private final String contentType;
    private final String desc;

    OssFileTypeEnum(String suffix, String contentType, String desc) {
        this.suffix = suffix;
        this.contentType = contentType;
        this.desc = desc;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getContentType() {
        return contentType;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据文件后缀获取类型
     */
    public static OssFileTypeEnum fromSuffix(String suffix) {
        if (suffix == null) {
            return OCTET_STREAM;
        }
        for (OssFileTypeEnum type : values()) {
            if (type.suffix.equalsIgnoreCase(suffix)) {
                return type;
            }
        }
        return OCTET_STREAM;
    }
}
