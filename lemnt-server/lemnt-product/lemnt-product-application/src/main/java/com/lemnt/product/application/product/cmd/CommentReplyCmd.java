package com.lemnt.product.application.product.cmd;

import java.io.Serializable;

/**
 * 商家评价回复。
 */
public record CommentReplyCmd(Long id, Long operatorId, String content) implements Serializable {
}
