package com.lemnt.mall.domain.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.model.ProductBrowseHistory;

/** mall_product_browse_history领域存储。 */
public interface ProductBrowseHistoryGateway {
    /** 替换同商品旧记录、保留最新且最多100条，在会员锁事务内调用。 */
    void record(Long userId, Long spuId);
    /** 隐藏本人指定商品；null仅由显式清空操作传入。 */
    void hide(Long userId, java.util.List<Long> spuIds);
    /** 管理端可查看隐藏记录，会员查询固定false。 */
    PageResult<ProductBrowseHistory> page(Long userId, Long spuId, Boolean userDeleted, int pageNum, int pageSize);
}
