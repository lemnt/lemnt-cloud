package com.lemnt.system.adapter.app.file.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 会员图片访问地址。 */
@Schema(description = "会员图片上传结果")
public record ImageUploadRes(@Schema(description = "图片访问地址") String url)
        implements Serializable {}
