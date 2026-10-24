package com.lemnt.trade.domain.gateway;
import com.lemnt.trade.domain.model.TradeOrder;
public interface TradeOrderGateway { TradeOrder save(TradeOrder order); TradeOrder findByOrderNo(String orderNo); }