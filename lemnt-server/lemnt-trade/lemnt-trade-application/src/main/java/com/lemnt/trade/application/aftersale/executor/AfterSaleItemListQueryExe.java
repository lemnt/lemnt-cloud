package com.lemnt.trade.application.aftersale.executor;

import com.lemnt.trade.application.aftersale.dto.AfterSaleItemDTO;
import com.lemnt.trade.application.aftersale.service.AfterSaleService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 商品售后额度查询。 */
@Component
@RequiredArgsConstructor
public class AfterSaleItemListQueryExe {
    private final AfterSaleService service;

    /** 查询创建售后所需的真实商品行ID和剩余额度。 */
    public List<AfterSaleItemDTO> execute(String orderNo) {
        return service.items(orderNo);
    }
}
