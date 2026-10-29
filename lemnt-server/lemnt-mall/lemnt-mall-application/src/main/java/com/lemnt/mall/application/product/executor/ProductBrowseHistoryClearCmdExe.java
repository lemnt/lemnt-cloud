package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.cmd.ProductBrowseHistoryClearCmd;
import com.lemnt.mall.application.product.service.ProductBrowseHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** clear执行器。 */
@Component
@RequiredArgsConstructor
public class ProductBrowseHistoryClearCmdExe {
    private final ProductBrowseHistoryService service;

    /** 按明确的用例边界执行。 */
    public boolean execute(ProductBrowseHistoryClearCmd value) {
        return service.clear(value.userId());
    }
}
