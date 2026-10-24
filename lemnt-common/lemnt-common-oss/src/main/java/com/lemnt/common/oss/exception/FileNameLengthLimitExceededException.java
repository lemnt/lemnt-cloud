package com.lemnt.common.oss.exception;

/**
 * 文件名称超长限制异常类
 *
 * @author wangxx
 */
public class FileNameLengthLimitExceededException extends FileException {
    private static final long serialVersionUID = 1L;

    public FileNameLengthLimitExceededException(int defaultFileNameLength) {
        super("upload.filename.exceed.length", new Object[]{defaultFileNameLength}, "the filename is too long");
    }
}
