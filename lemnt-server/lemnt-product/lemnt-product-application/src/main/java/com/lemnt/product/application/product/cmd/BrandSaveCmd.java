package com.lemnt.product.application.product.cmd;

import java.io.Serializable;

/**
 * 品牌保存命令。
 */
public record BrandSaveCmd(
        Long id, String name, String picUrl, String description, Integer sort, String status)
        implements Serializable {}
