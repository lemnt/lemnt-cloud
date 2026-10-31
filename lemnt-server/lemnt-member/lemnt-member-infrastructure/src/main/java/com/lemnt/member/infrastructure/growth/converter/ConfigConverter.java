package com.lemnt.member.infrastructure.growth.converter;

import com.lemnt.member.domain.growth.model.Config;
import com.lemnt.member.infrastructure.growth.dataobject.ConfigDO;

/**
 * 配置转换器。
 */
public final class ConfigConverter {
    private ConfigConverter() {}

    /**
     * 转换为领域对象。
     */
    public static Config toEntity(ConfigDO row) {
        return row == null
                ? null
                : new Config(
                        row.getPointTradeDeductEnable(),
                        row.getPointTradeDeductUnitPrice(),
                        row.getPointTradeDeductMaxPrice(),
                        row.getPointTradeGivePoint());
    }

    /**
     * 转换为持久化数据对象。
     */
    public static ConfigDO toDO(Config value) {
        var row = new ConfigDO();
        row.setPointTradeDeductEnable(value.pointTradeDeductEnable());
        row.setPointTradeDeductUnitPrice(value.pointTradeDeductUnitPrice());
        row.setPointTradeDeductMaxPrice(value.pointTradeDeductMaxPrice());
        row.setPointTradeGivePoint(value.pointTradeGivePoint());
        return row;
    }
}
