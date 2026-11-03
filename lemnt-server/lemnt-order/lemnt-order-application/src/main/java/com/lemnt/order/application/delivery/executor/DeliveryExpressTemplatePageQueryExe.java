package com.lemnt.order.application.delivery.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.order.application.delivery.converter.DeliveryDTOConverter;
import com.lemnt.order.application.delivery.dto.DeliveryExpressTemplateDTO;
import com.lemnt.order.application.delivery.qry.DeliveryExpressTemplatePageQry;
import com.lemnt.order.domain.delivery.gateway.DeliveryExpressTemplateGateway;
import com.lemnt.order.domain.delivery.model.DeliveryExpressTemplateFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 配送分页执行器。 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressTemplatePageQueryExe {
    private final DeliveryExpressTemplateGateway gateway;

    /** 执行配送操作。 */
    public PageResult<DeliveryExpressTemplateDTO> execute(DeliveryExpressTemplatePageQry qry) {
        if (qry.pageNum() == null
                || qry.pageNum() < 1
                || qry.pageSize() == null
                || qry.pageSize() < 1
                || qry.pageSize() > 100) {
            throw new ServiceException("分页参数不合法", 400);
        }
        var page =
                gateway.page(
                        new DeliveryExpressTemplateFilter(
                                qry.name(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(
                page.getRecords().stream().map(DeliveryDTOConverter::toDTO).toList(),
                page.getTotal());
    }
}
