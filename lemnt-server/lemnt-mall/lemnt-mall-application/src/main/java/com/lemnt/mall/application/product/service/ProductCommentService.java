package com.lemnt.mall.application.product.service;

import com.lemnt.mall.application.product.cmd.CommentCreateCmd;
import com.lemnt.mall.application.product.cmd.CommentReplyCmd;
import com.lemnt.mall.domain.product.gateway.ProductCommentGateway;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductComment;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/** 商品评价只来自真实完成订单，后台不能伪造会员和订单评价。 */
@Component
@RequiredArgsConstructor
public class ProductCommentService {
    private final ProductCommentGateway productCommentGateway;
    private final ProductSkuGateway productSkuGateway;
    private final OrderGateway orderGateway;

    /** 创建评价，原请求重试先核验已有内容，远程订单查询失败不产生评价。 */
    public ProductComment create(CommentCreateCmd cmd) {
        if (cmd == null || cmd.score() == null || cmd.score() < 1 || cmd.score() > 5) {
            throw new IllegalArgumentException("评分必须为1至5分");
        }
        ProductCatalogValidation.id(cmd.memberId());
        ProductCatalogValidation.id(cmd.skuId());
        String no = ProductCatalogValidation.text(cmd.orderNo(), 40, "订单编号");
        String content = ProductCatalogValidation.text(cmd.content(), 1000, "评价内容");
        var existing = productCommentGateway.findByOrderSku(no, cmd.skuId());
        if (existing != null) {
            return requireSame(existing, cmd.memberId(), cmd.score(), content);
        }
        var order = orderGateway.findByOrderNo(no);
        if (order == null || !cmd.memberId().equals(order.memberId()) || !"COMPLETED".equals(order.status())
                || order.items().stream().noneMatch(item -> cmd.skuId().equals(item.productId()))) {
            throw new IllegalArgumentException("只能评价本人已完成订单内的商品");
        }
        var sku = productSkuGateway.catalogSku(cmd.skuId());
        if (sku == null || sku.spuId() == null) {
            throw new IllegalArgumentException("订单商品快照不存在");
        }
        try {
            return productCommentGateway.save(new ProductComment(null, sku.spuId(), sku.id(), no, cmd.memberId(), cmd.score(),
                content,
                    true, null, null, null, null));
        } catch (DuplicateKeyException exception) {
            var winner = productCommentGateway.findByOrderSku(no, cmd.skuId());
            if (winner == null) {
                throw exception;
            }
            return requireSame(winner, cmd.memberId(), cmd.score(), content);
        }
    }

    private ProductComment requireSame(ProductComment value, Long memberId, Integer score, String content) {
        if (!memberId.equals(value.memberId())) {
            throw new IllegalArgumentException("只能评价本人已完成订单内的商品");
        }
        if (!score.equals(value.score()) || !content.equals(value.content())) {
            throw new IllegalStateException("该订单商品已评价，不能改写或重复评价");
        }
        return value;
    }

    /** 商家回复保留操作者，按参考项目允许更新回复，相同内容重试保留时间。 */
    @Transactional(rollbackFor = Exception.class)
    public ProductComment reply(CommentReplyCmd cmd) {
        ProductCatalogValidation.id(cmd.id());
        ProductCatalogValidation.id(cmd.operatorId());
        String content = ProductCatalogValidation.text(cmd.content(), 1000, "回复内容");
        var value = productCommentGateway.lock(cmd.id());
        if (value == null) {
            throw new IllegalArgumentException("评价不存在");
        }
        if (content.equals(value.replyContent())) {
            return value;
        }
        var updated = new ProductComment(value.id(), value.spuId(), value.skuId(), value.orderNo(), value.memberId(),
                value.score(), value.content(), value.visible(), content, cmd.operatorId(), LocalDateTime.now(), value.createTime());
        if (!productCommentGateway.reply(updated)) {
            throw new IllegalStateException("回复保存失败");
        }
        return productCommentGateway.find(value.id());
    }
}
