package com.lemnt.trade.infrastructure.config.converter;

import com.lemnt.trade.domain.config.model.TradeConfig;
import com.lemnt.trade.infrastructure.config.dataobject.TradeConfigDO;

import com.alibaba.fastjson2.JSON;

/** 交易配置 与表字段转换。 */
public final class TradeConfigConverter {
    private TradeConfigConverter() {
    }

    /** 读取业务对象。 */
    public static TradeConfig toEntity(TradeConfigDO row) {
        if (row == null) {
            return null;
        }
        return new TradeConfig(
                row.getId(),
                JSON.parseArray(row.getAfterSaleRefundReasons(), String.class),
                JSON.parseArray(row.getAfterSaleReturnReasons(), String.class),
                row.getDeliveryExpressFreeEnabled(),
                row.getDeliveryExpressFreePrice(),
                row.getDeliveryPickUpEnabled(),
                row.getBrokerageEnabled(),
                row.getBrokerageEnabledCondition(),
                row.getBrokerageBindMode(),
                JSON.parseArray(row.getBrokeragePosterUrls(), String.class),
                row.getBrokerageFirstPercent(),
                row.getBrokerageSecondPercent(),
                row.getBrokerageWithdrawMinPrice(),
                row.getBrokerageWithdrawFeePercent(),
                row.getBrokerageFrozenDays(),
                JSON.parseArray(row.getBrokerageWithdrawTypes(), Integer.class));
    }

    /** 写入表字段，集合列按 JSON 保存。 */
    public static TradeConfigDO toDO(TradeConfig value) {
        var row = new TradeConfigDO();
        row.setId(value.id());
        row.setAfterSaleRefundReasons(JSON.toJSONString(value.afterSaleRefundReasons()));
        row.setAfterSaleReturnReasons(JSON.toJSONString(value.afterSaleReturnReasons()));
        row.setDeliveryExpressFreeEnabled(value.deliveryExpressFreeEnabled());
        row.setDeliveryExpressFreePrice(value.deliveryExpressFreePrice());
        row.setDeliveryPickUpEnabled(value.deliveryPickUpEnabled());
        row.setBrokerageEnabled(value.brokerageEnabled());
        row.setBrokerageEnabledCondition(value.brokerageEnabledCondition());
        row.setBrokerageBindMode(value.brokerageBindMode());
        row.setBrokeragePosterUrls(JSON.toJSONString(value.brokeragePosterUrls()));
        row.setBrokerageFirstPercent(value.brokerageFirstPercent());
        row.setBrokerageSecondPercent(value.brokerageSecondPercent());
        row.setBrokerageWithdrawMinPrice(value.brokerageWithdrawMinPrice());
        row.setBrokerageWithdrawFeePercent(value.brokerageWithdrawFeePercent());
        row.setBrokerageFrozenDays(value.brokerageFrozenDays());
        row.setBrokerageWithdrawTypes(JSON.toJSONString(value.brokerageWithdrawTypes()));
        return row;
    }
}
