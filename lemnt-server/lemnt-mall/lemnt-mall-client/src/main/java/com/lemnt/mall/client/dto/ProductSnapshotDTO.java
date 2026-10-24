package com.lemnt.mall.client.dto;
import java.io.Serializable;
import java.math.BigDecimal;
public record ProductSnapshotDTO(Long id,String productCode,String name,BigDecimal price,Integer stock) implements Serializable { }