package com.lemnt.mall.application.product.cmd;

import java.io.Serializable;

/** 评价显示设置。 */
public record CommentVisibleCmd(
        Long id,
        boolean visible
) implements Serializable {
}
