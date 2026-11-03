package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.qry.ProductBrowseHistoryPageQry;
import com.lemnt.product.application.product.service.ProductBrowseHistoryService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** BrowseHistorypage执行器。 */
@Component
@RequiredArgsConstructor
public class ProductBrowseHistoryPageQueryExe {
    private final ProductBrowseHistoryService service;

    /** 委托领域应用服务完成本次用例。 */
    public com.lemnt.common.domain.model.PageResult<
                    com.lemnt.product.application.product.dto.ProductBrowseHistoryDTO>
            execute(ProductBrowseHistoryPageQry value) {
        return service.page(value);
    }
}
