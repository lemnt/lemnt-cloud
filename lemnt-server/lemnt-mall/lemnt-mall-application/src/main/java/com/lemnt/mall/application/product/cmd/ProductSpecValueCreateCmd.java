package com.lemnt.mall.application.product.cmd;

import java.io.Serializable;

/** 创建规格值。 */
public record ProductSpecValueCreateCmd(
        Long specId,
        String name,
        String remark
) implements Serializable {
}
