package com.lemnt.mall.infrastructure.product.gateway;

import com.lemnt.mall.domain.product.model.PublicCommentFilter;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.gateway.ProductCommentGateway;
import com.lemnt.mall.domain.product.model.CommentFilter;
import com.lemnt.mall.domain.product.model.ProductComment;
import com.lemnt.mall.infrastructure.product.converter.ProductCommentConverter;
import com.lemnt.mall.infrastructure.product.dataobject.ProductCommentDO;
import com.lemnt.mall.infrastructure.product.mapper.ProductCommentMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 商品评价表存储实现。 */
@Component
@RequiredArgsConstructor
public class ProductCommentGatewayImpl implements ProductCommentGateway {
    private final ProductCommentMapper productCommentMapper;
    @Override
    public ProductComment find(Long id) {
        return ProductCommentConverter.toModel(productCommentMapper.selectById(id));
    }

    @Override
    public ProductComment lock(Long id) {
        return ProductCommentConverter.toModel(productCommentMapper.lock(id));
    }

    @Override
    public ProductComment findByOrderSku(String orderNo, Long skuId) {
        var legacy = productCommentMapper.legacyForLink(orderNo, skuId);
        if (legacy.size() > 1 || legacy.stream().anyMatch(value -> Boolean.TRUE.equals(value.getDeleted()))) {
            throw new com.lemnt.common.core.exception.ServiceException("旧评价已删除或归属不唯一，不能重复投稿", 409);
        }
        return legacy.isEmpty() ? null : ProductCommentConverter.toModel(legacy.get(0));
    }

    @Override
    public ProductComment save(ProductComment value) {
        var row = ProductCommentConverter.toDO(value);
        productCommentMapper.insert(row);
        return find(row.getId());
    }

    @Override
    public boolean reply(ProductComment value) {
        return productCommentMapper.update(null, Wrappers.lambdaUpdate(ProductCommentDO.class)
                .eq(ProductCommentDO::getId, value.id())
                .set(ProductCommentDO::getReplyContent, value.replyContent())
                .set(ProductCommentDO::getReplyUserId, value.replyUserId())
                .set(ProductCommentDO::getReplyTime, value.replyTime())
                .set(ProductCommentDO::getReplyStatus, true)) == 1;
    }

    @Override
    public boolean visible(Long id, boolean visible) {
        return productCommentMapper.update(null, Wrappers.lambdaUpdate(ProductCommentDO.class)
                .eq(ProductCommentDO::getId, id).set(ProductCommentDO::getVisible, visible)) == 1;
    }

    @Override
    public PageResult<ProductComment> page(CommentFilter filter) {
        var page = productCommentMapper.selectPage(new Page<>(filter.pageNum(), filter.pageSize()),
            Wrappers.lambdaQuery(ProductCommentDO.class)
                .eq(filter.spuId() != null, ProductCommentDO::getSpuId, filter.spuId())
                .eq(filter.memberId() != null, ProductCommentDO::getMemberId, filter.memberId())
                .eq(filter.score() != null, ProductCommentDO::getScore, filter.score())
                .eq(filter.visible() != null, ProductCommentDO::getVisible, filter.visible())
                .isNotNull(Boolean.TRUE.equals(filter.replied()), ProductCommentDO::getReplyContent)
                .isNull(Boolean.FALSE.equals(filter.replied()), ProductCommentDO::getReplyContent)
                .orderByDesc(ProductCommentDO::getId));
        return PageResult.of(page.getRecords().stream().map(ProductCommentConverter::toModel).toList(),
            page.getTotal());
    }
    @Override
    public PageResult<ProductComment> pageVisible(PublicCommentFilter filter) {
        var query = Wrappers.lambdaQuery(ProductCommentDO.class)
                .eq(ProductCommentDO::getSpuId, filter.spuId()).eq(ProductCommentDO::getVisible, true)
                .ge(filter.type() == 1, ProductCommentDO::getScore, 4)
                .eq(filter.type() == 2, ProductCommentDO::getScore, 3)
                .lt(filter.type() == 3, ProductCommentDO::getScore, 3)
                .orderByDesc(ProductCommentDO::getCreateTime).orderByDesc(ProductCommentDO::getId);
        var result = productCommentMapper.selectPage(new Page<>(filter.pageNum(), filter.pageSize()), query);
        return PageResult.of(result.getRecords().stream().map(ProductCommentConverter::toModel).toList(), result.getTotal());
    }
    @Override
    public ProductComment findByOrderItem(Long orderItemId) {
        return ProductCommentConverter.toModel(productCommentMapper.selectOne(Wrappers.lambdaQuery(ProductCommentDO.class)
                .eq(ProductCommentDO::getOrderItemId, orderItemId)));
    }

    @Override
    public boolean bindOrderItem(com.lemnt.mall.domain.product.model.CommentOrderLink value) {
        return productCommentMapper.update(null, Wrappers.lambdaUpdate(ProductCommentDO.class)
                .eq(ProductCommentDO::getId, value.commentId())
                .and(query -> query.isNull(ProductCommentDO::getOrderItemId).or().eq(ProductCommentDO::getOrderItemId, value.orderItemId()))
                .and(query -> query.isNull(ProductCommentDO::getOrderId).or().eq(ProductCommentDO::getOrderId, value.orderId()))
                .set(ProductCommentDO::getOrderItemId, value.orderItemId()).set(ProductCommentDO::getOrderId, value.orderId())) == 1;
    }
}
