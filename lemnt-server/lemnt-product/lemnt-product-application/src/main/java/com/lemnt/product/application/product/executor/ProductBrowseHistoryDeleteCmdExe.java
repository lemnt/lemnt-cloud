package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.cmd.ProductBrowseHistoryDeleteCmd;
import com.lemnt.product.application.product.service.ProductBrowseHistoryService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** hide执行器。 */
@Component
@RequiredArgsConstructor
public class ProductBrowseHistoryDeleteCmdExe {
    private final ProductBrowseHistoryService service;

    /** 按明确的用例边界执行。 */
    public boolean execute(ProductBrowseHistoryDeleteCmd value) {
        return service.hide(value.userId(), value.spuIds());
    }
}
