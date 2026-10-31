package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.qry.ProductBrowseHistoryPageQry;
import com.lemnt.mall.application.product.service.ProductBrowseHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** BrowseHistorypage执行器。 */
@Component
@RequiredArgsConstructor
public class ProductBrowseHistoryPageQueryExe {
    private final ProductBrowseHistoryService service;

    /** 委托领域应用服务完成本次用例。 */
    public com.lemnt.common.domain.model.PageResult<com.lemnt.mall.application.product.dto.ProductBrowseHistoryDTO> execute(ProductBrowseHistoryPageQry value) {
        return service.page(value);
    }
}
