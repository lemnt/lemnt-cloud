package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.cmd.ProductBrowseHistoryCreateCmd;
import com.lemnt.product.application.product.service.ProductBrowseHistoryService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

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
