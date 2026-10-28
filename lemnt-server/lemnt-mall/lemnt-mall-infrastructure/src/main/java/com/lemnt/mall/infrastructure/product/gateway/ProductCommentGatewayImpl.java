package com.lemnt.mall.infrastructure.product.gateway;

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
        return ProductCommentConverter.toModel(productCommentMapper.selectOne(Wrappers.lambdaQuery(ProductCommentDO.class)
                .eq(ProductCommentDO::getOrderNo, orderNo).eq(ProductCommentDO::getSkuId, skuId)));
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
                .set(ProductCommentDO::getReplyTime, value.replyTime())) == 1;
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
}
