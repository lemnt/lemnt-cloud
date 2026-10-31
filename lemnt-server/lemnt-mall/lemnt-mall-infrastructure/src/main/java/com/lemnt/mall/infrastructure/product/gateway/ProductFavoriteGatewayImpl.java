package com.lemnt.mall.infrastructure.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.gateway.ProductFavoriteGateway;
import com.lemnt.mall.domain.product.model.ProductFavorite;
import com.lemnt.mall.infrastructure.product.dataobject.ProductFavoriteDO;
import com.lemnt.mall.infrastructure.product.mapper.ProductFavoriteMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** mall_product_favorite持久化，仅通过BaseMapper与Lambda读写。 */
@Component
@RequiredArgsConstructor
public class ProductFavoriteGatewayImpl implements ProductFavoriteGateway {
    private final ProductFavoriteMapper mapper;
    @Override
    public ProductFavorite find(Long userId, Long spuId) {
        return model(mapper.selectOne(Wrappers.lambdaQuery(ProductFavoriteDO.class)
                .eq(ProductFavoriteDO::getUserId, userId).eq(ProductFavoriteDO::getSpuId, spuId)));
    }
    @Override
    public ProductFavorite save(Long userId, Long spuId) {
        var row = new ProductFavoriteDO().setUserId(userId).setSpuId(spuId);
        mapper.insert(row);
        return model(mapper.selectById(row.getId()));
    }
    @Override
    public boolean remove(Long userId, Long spuId) {
        return mapper.delete(Wrappers.lambdaQuery(ProductFavoriteDO.class)
                .eq(ProductFavoriteDO::getUserId, userId).eq(ProductFavoriteDO::getSpuId, spuId)) == 1;
    }
    @Override
    public long count(Long userId) {
        return mapper.selectCount(Wrappers.lambdaQuery(ProductFavoriteDO.class).eq(ProductFavoriteDO::getUserId, userId));
    }

    @Override
    public PageResult<ProductFavorite> page(Long userId, Long spuId, int pageNum, int pageSize) {
        var page = mapper.selectPage(new Page<>(pageNum, pageSize), Wrappers.lambdaQuery(ProductFavoriteDO.class)
                .eq(userId != null, ProductFavoriteDO::getUserId, userId)
                .eq(spuId != null, ProductFavoriteDO::getSpuId, spuId)
                
                .orderByDesc(ProductFavoriteDO::getId));
        return PageResult.of(page.getRecords().stream().map(this::model).toList(), page.getTotal());
    }

    private ProductFavorite model(ProductFavoriteDO row) {
        return row == null ? null : new ProductFavorite(row.getId(), row.getUserId(), row.getSpuId(),
                row.getCreateTime());
    }
}
