package com.lemnt.mall.application.cmd;
import java.math.BigDecimal;
public record ProductCreateCmd(String productCode,String name,String description,String mainImage,BigDecimal price,Integer stock) { }