package com.lemnt.trade.application.delivery.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.delivery.converter.DeliveryDTOConverter;
import com.lemnt.trade.application.delivery.dto.DeliveryExpressDTO;
import com.lemnt.trade.application.delivery.qry.DeliveryExpressPageQry;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressGateway;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 配送分页执行器。 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressPageQueryExe {
    private final DeliveryExpressGateway gateway;

    /** 执行配送操作。 */
    public PageResult<DeliveryExpressDTO> execute(DeliveryExpressPageQry qry) {
        if (qry.pageNum() == null || qry.pageNum() < 1 || qry.pageSize() == null
                || qry.pageSize() < 1 || qry.pageSize() > 100) {
            throw new ServiceException("分页参数不合法", 400);
        }
        var page = gateway.page(new DeliveryExpressFilter(qry.name(), qry.status(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(DeliveryDTOConverter::toDTO).toList(), page.getTotal());
    }
}
