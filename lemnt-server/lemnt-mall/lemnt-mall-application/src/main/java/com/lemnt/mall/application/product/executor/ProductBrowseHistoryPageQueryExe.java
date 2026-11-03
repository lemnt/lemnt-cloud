package com.lemnt.mall.application.product.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.product.dto.ProductBrowseHistoryDTO;
import com.lemnt.mall.application.product.qry.ProductBrowseHistoryPageQry;
import com.lemnt.mall.application.product.service.ProductBrowseHistoryService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** BrowseHistorypage执行器。 */
@Component
@RequiredArgsConstructor
public class ProductBrowseHistoryPageQueryExe {
    private final ProductBrowseHistoryService service;

    /** 委托领域应用服务完成本次用例。 */
    public PageResult<
                    ProductBrowseHistoryDTO>
            execute(ProductBrowseHistoryPageQry value) {
        return service.page(value);
    }
}
