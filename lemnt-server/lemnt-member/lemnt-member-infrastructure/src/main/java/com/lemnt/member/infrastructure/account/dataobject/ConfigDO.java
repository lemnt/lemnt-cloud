package com.lemnt.member.infrastructure.account.dataobject;

@lombok.Data
public class ConfigDO {
    private Boolean pointTradeDeductEnable;
    private Integer pointTradeDeductUnitPrice;
    private Integer pointTradeDeductMaxPrice;
    private Integer pointTradeGivePoint;
}
