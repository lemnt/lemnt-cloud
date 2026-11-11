package com.lemnt.product.application.product.cmd;

import java.io.Serializable;

/**
 * 分类保存命令。
 */
public record ProductCategorySaveCmd(
    Long id, Long parentId, String name, String picUrl, Integer sort, String status)
    implements Serializable {
}
