package com.lemnt.trade.application.cmd;
import java.util.List;
public record TradeOrderCreateCmd(Long memberId,List<OrderLine> items) { public record OrderLine(Long productId,Integer quantity) { } }