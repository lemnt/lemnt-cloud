package com.lemnt.mall.application.product.cmd;

import java.io.Serializable;

/** 修改规格项。 */
public record ProductSpecUpdateCmd(
        Long id,
        String name,
        String remark
) implements Serializable {
}
