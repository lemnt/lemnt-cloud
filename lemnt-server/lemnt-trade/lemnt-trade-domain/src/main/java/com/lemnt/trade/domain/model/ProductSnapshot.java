package com.lemnt.trade.domain.model;
import java.math.BigDecimal;
public record ProductSnapshot(Long id,String productCode,String name,BigDecimal price,Integer stock) { }