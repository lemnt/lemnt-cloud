package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.cmd.*;
import com.lemnt.mall.application.catalog.dto.*;
import com.lemnt.mall.application.catalog.converter.CatalogDTOConverter;
import com.lemnt.mall.domain.catalog.gateway.CatalogGateway;
import com.lemnt.mall.domain.catalog.model.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BrandSaveCmdExe {
    private final CatalogGateway gateway;

    @Transactional(rollbackFor = Exception.class)
    public BrandDTO execute(BrandSaveCmd cmd) {
        if (cmd == null) throw new IllegalArgumentException("品牌参数不能为空");
        if (cmd.id() != null) CatalogRules.id(cmd.id());
        if (cmd.sort() == null || cmd.sort() < 0) throw new IllegalArgumentException("排序不能小于0");
        var brand =
                new Brand(
                        cmd.id(),
                        CatalogRules.text(cmd.name(), 100, "品牌名称"),
                        CatalogRules.optional(cmd.picUrl(), 500),
                        CatalogRules.optional(cmd.description(), 2000),
                        cmd.sort(),
                        CatalogRules.status(cmd.status()));
        return CatalogDTOConverter.brand(gateway.saveBrand(brand));
    }
}
