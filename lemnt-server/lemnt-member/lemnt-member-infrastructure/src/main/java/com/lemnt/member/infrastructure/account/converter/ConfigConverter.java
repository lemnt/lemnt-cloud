package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.Config;
import com.lemnt.member.infrastructure.account.dataobject.ConfigDO;

public final class ConfigConverter {
    private ConfigConverter() {}

    public static Config domain(ConfigDO row) {
        return row == null
                ? null
                : new Config(
                        row.getPointTradeDeductEnable(),
                        row.getPointTradeDeductUnitPrice(),
                        row.getPointTradeDeductMaxPrice(),
                        row.getPointTradeGivePoint());
    }

    public static ConfigDO row(Config value) {
        var row = new ConfigDO();
        row.setPointTradeDeductEnable(value.pointTradeDeductEnable());
        row.setPointTradeDeductUnitPrice(value.pointTradeDeductUnitPrice());
        row.setPointTradeDeductMaxPrice(value.pointTradeDeductMaxPrice());
        row.setPointTradeGivePoint(value.pointTradeGivePoint());
        return row;
    }
}
