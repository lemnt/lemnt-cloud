package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.cmd.CommentReplyCmd;
import com.lemnt.product.application.product.converter.CommentDTOConverter;
import com.lemnt.product.application.product.dto.ProductCommentDTO;
import com.lemnt.product.application.product.service.ProductCommentService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 商家回复执行器。
 */
@Component
@RequiredArgsConstructor
public class CommentReplyCmdExe {
    private final ProductCommentService productCommentService;

    /**
     * 执行评价写入并返回应用结果。
     */
    public ProductCommentDTO execute(CommentReplyCmd value) {
        return CommentDTOConverter.toDTO(productCommentService.reply(value));
    }
}
