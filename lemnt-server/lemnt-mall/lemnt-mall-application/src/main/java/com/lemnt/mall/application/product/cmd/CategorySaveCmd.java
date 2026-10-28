package com.lemnt.mall.application.product.cmd;

/**
 * 分类保存命令。
 */
public record CategorySaveCmd(
        Long id,
        Long parentId,
        String name,
        String picUrl,
        Integer sort,
        String status
) implements java.io.Serializable {}
