package com.lemnt.mall.infrastructure.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.gateway.ProductBrowseHistoryGateway;
import com.lemnt.mall.domain.product.model.ProductBrowseHistory;
import com.lemnt.mall.infrastructure.product.dataobject.ProductBrowseHistoryDO;
import com.lemnt.mall.infrastructure.product.mapper.ProductBrowseHistoryMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** mall_product_browse_history持久化，仅通过BaseMapper与Lambda读写。 */
@Component
@RequiredArgsConstructor
public class ProductBrowseHistoryGatewayImpl implements ProductBrowseHistoryGateway {
    private final ProductBrowseHistoryMapper mapper;
    @Override
    public void record(Long userId, Long spuId) {
        mapper.delete(Wrappers.lambdaQuery(ProductBrowseHistoryDO.class)
                .eq(ProductBrowseHistoryDO::getUserId, userId).eq(ProductBrowseHistoryDO::getSpuId, spuId));
        var oldest = mapper.selectPage(new Page<>(1, 1), Wrappers.lambdaQuery(ProductBrowseHistoryDO.class)
                .eq(ProductBrowseHistoryDO::getUserId, userId).orderByAsc(ProductBrowseHistoryDO::getId));
        if (oldest.getTotal() >= 100) {
            mapper.deleteById(oldest.getRecords().get(0).getId());
        }
        mapper.insert(new ProductBrowseHistoryDO().setUserId(userId).setSpuId(spuId).setUserDeleted(false));
    }
    @Override
    public void hide(Long userId, java.util.List<Long> spuIds) {
        mapper.update(null, Wrappers.lambdaUpdate(ProductBrowseHistoryDO.class)
                .eq(ProductBrowseHistoryDO::getUserId, userId)
                .in(spuIds != null, ProductBrowseHistoryDO::getSpuId, spuIds)
                .set(ProductBrowseHistoryDO::getUserDeleted, true));
    }

    @Override
    public PageResult<ProductBrowseHistory> page(Long userId, Long spuId, Boolean userDeleted, int pageNum, int pageSize) {
        var page = mapper.selectPage(new Page<>(pageNum, pageSize), Wrappers.lambdaQuery(ProductBrowseHistoryDO.class)
                .eq(userId != null, ProductBrowseHistoryDO::getUserId, userId)
                .eq(spuId != null, ProductBrowseHistoryDO::getSpuId, spuId)
                .eq(userDeleted != null, ProductBrowseHistoryDO::getUserDeleted, userDeleted)
                .orderByDesc(ProductBrowseHistoryDO::getId));
        return PageResult.of(page.getRecords().stream().map(this::model).toList(), page.getTotal());
    }

    private ProductBrowseHistory model(ProductBrowseHistoryDO row) {
        return row == null ? null : new ProductBrowseHistory(row.getId(), row.getUserId(), row.getSpuId(),
                Boolean.TRUE.equals(row.getUserDeleted()), row.getCreateTime());
    }
}
