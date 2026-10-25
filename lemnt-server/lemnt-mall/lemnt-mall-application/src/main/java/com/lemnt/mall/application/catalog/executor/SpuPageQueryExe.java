package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.cmd.*;
import com.lemnt.mall.application.catalog.dto.*;
import com.lemnt.mall.application.catalog.converter.CatalogDTOConverter;
import com.lemnt.mall.domain.catalog.gateway.CatalogGateway;
import com.lemnt.mall.domain.catalog.model.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import com.lemnt.mall.application.catalog.qry.SpuPageQry;

@Component
@RequiredArgsConstructor
public class SpuPageQueryExe {
    private final CatalogGateway gateway;

    public SpuPageDTO execute(SpuPageQry query) {
        if (query == null
                || query.pageNum() < 1
                || query.pageNum() > 1000000
                || query.pageSize() < 1
                || query.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        String name =
                query.name() == null || query.name().isBlank()
                        ? null
                        : CatalogRules.text(query.name(), 200, "商品名称");
        String status = query.status() == null || query.status().isBlank() ? null : query.status();
        if (status != null && !java.util.Set.of("ON_SHELF", "OFF_SHELF").contains(status)) {
            throw new IllegalArgumentException("商品状态不合法");
        }
        var page =
                gateway.page(
                        new CatalogFilter(
                                name,
                                status,
                                (query.pageNum() - 1) * query.pageSize(),
                                query.pageSize()));
        return new SpuPageDTO(
                page.records().stream().map(CatalogDTOConverter::spu).toList(), page.total());
    }
}
