package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.cmd.ProductFavoriteDeleteCmd;
import com.lemnt.mall.application.product.service.ProductFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** remove执行器。 */
@Component
@RequiredArgsConstructor
public class ProductFavoriteDeleteCmdExe {
    private final ProductFavoriteService service;

    /** 按明确的用例边界执行。 */
    public boolean execute(ProductFavoriteDeleteCmd value) {
        return service.remove(value.userId(), value.spuId());
    }
}
