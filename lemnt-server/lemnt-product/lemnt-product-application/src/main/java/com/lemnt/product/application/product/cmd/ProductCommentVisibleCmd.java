package com.lemnt.product.application.product.cmd;

import java.io.Serializable;

/**
 * 评价显示设置。
 */
public record ProductCommentVisibleCmd(Long id, boolean visible) implements Serializable {
}
