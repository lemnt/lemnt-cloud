package com.lemnt.trade.application.aftersale.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.aftersale.dto.AfterSaleLogDTO;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleLogGateway;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 后台售后真实操作日志查询。 */
@Component
@RequiredArgsConstructor
public class AfterSaleLogListQueryExe {
    private final AfterSaleGateway afterSales;
    private final AfterSaleLogGateway logs;
    /** 以售后数值主键读取日志。 */
    public List<AfterSaleLogDTO> execute(String no) {
        var value = afterSales.afterSale(OrderRules.text(no, 40));
        if (value == null) { throw new ServiceException("售后单不存在", 400); }
        return logs.byAfterSaleId(value.id()).stream().map(row -> new AfterSaleLogDTO(row.id(), row.userId(), row.userType(),
                row.afterSaleId(), row.beforeStatus(), row.afterStatus(), row.operateType(), row.content(), row.createTime())).toList();
    }
}
