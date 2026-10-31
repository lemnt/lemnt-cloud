package com.lemnt.mall.application.product.cmd;

import java.io.Serializable;

/** 创建规格项。 */
public record ProductSpecCreateCmd(
        String name,
        String remark
) implements Serializable {
}
