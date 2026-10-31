package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.cmd.ProductBrowseHistoryCreateCmd;
import com.lemnt.mall.application.product.service.ProductBrowseHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** BrowseHistorycreate执行器。 */
@Component
@RequiredArgsConstructor
public class ProductBrowseHistoryCreateCmdExe {
    private final ProductBrowseHistoryService service;

    /** 委托领域应用服务完成本次用例。 */
    public boolean execute(ProductBrowseHistoryCreateCmd value) {
        return service.create(value);
    }
}
