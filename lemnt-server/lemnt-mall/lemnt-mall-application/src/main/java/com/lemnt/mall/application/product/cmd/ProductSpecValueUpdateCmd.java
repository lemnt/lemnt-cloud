package com.lemnt.mall.application.product.cmd;

import java.io.Serializable;

/** 修改规格值。 */
public record ProductSpecValueUpdateCmd(
        Long id,
        String name,
        String remark
) implements Serializable {
}
