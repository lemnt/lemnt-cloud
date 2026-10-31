package com.lemnt.trade.application.config.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.config.cmd.TradeConfigUpdateCmd;
import com.lemnt.trade.domain.config.gateway.TradeConfigGateway;
import com.lemnt.trade.domain.config.model.TradeConfig;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 交易配置的单例更新，金额保持参考项目的整数分单位。 */
@Component
@RequiredArgsConstructor
public class TradeConfigService {
    private final TradeConfigGateway gateway;

    /** 更新完整配置并保留分销字段，不代表分销流程已交付。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean update(TradeConfigUpdateCmd cmd) {
        validateReasons(cmd.afterSaleRefundReasons());
        validateReasons(cmd.afterSaleReturnReasons());
        require(cmd.deliveryExpressFreeEnabled() != null && cmd.deliveryPickUpEnabled() != null
                && cmd.brokerageEnabled() != null, "配置开关不能为空");
        require(cmd.deliveryExpressFreePrice() != null && cmd.deliveryExpressFreePrice() >= 0,
                "全场包邮金额不合法");
        require(cmd.brokerageEnabledCondition() != null && List.of(1, 2).contains(cmd.brokerageEnabledCondition())
                && cmd.brokerageBindMode() != null && List.of(1, 2, 3).contains(cmd.brokerageBindMode()),
                "分销模式不合法");
        percent(cmd.brokerageFirstPercent());
        percent(cmd.brokerageSecondPercent());
        percent(cmd.brokerageWithdrawFeePercent());
        require(cmd.brokerageFirstPercent() + cmd.brokerageSecondPercent() <= 100, "总返佣比例不能超过百分之百");
        require(cmd.brokerageWithdrawMinPrice() != null && cmd.brokerageWithdrawMinPrice() >= 0
                && cmd.brokerageFrozenDays() != null && cmd.brokerageFrozenDays() >= 0, "提现或冻结配置不合法");
        require(cmd.brokeragePosterUrls() != null && cmd.brokeragePosterUrls().size() <= 100
                && cmd.brokeragePosterUrls().stream().allMatch(value -> value != null && !value.isBlank()
                        && value.length() <= 500), "分销海报地址不合法");
        require(cmd.brokerageWithdrawTypes() != null && cmd.brokerageWithdrawTypes().size() <= 20
                && cmd.brokerageWithdrawTypes().stream().allMatch(value -> value != null && List.of(1, 2, 3, 4, 5, 6).contains(value)),
                "提现方式不合法");
        gateway.lock();
        gateway.update(new TradeConfig(
                1L,
                cmd.afterSaleRefundReasons(),
                cmd.afterSaleReturnReasons(),
                cmd.deliveryExpressFreeEnabled(),
                cmd.deliveryExpressFreePrice(),
                cmd.deliveryPickUpEnabled(),
                cmd.brokerageEnabled(),
                cmd.brokerageEnabledCondition(),
                cmd.brokerageBindMode(),
                cmd.brokeragePosterUrls(),
                cmd.brokerageFirstPercent(),
                cmd.brokerageSecondPercent(),
                cmd.brokerageWithdrawMinPrice(),
                cmd.brokerageWithdrawFeePercent(),
                cmd.brokerageFrozenDays(),
                cmd.brokerageWithdrawTypes()));
        return true;
    }

    private void validateReasons(List<String> reasons) {
        require(reasons != null && reasons.size() <= 100 && reasons.stream().allMatch(value -> value != null
                && !value.isBlank() && value.length() <= 200), "售后原因配置不合法");
        require(reasons.stream().distinct().count() == reasons.size(), "售后原因不能重复");
    }

    private void percent(Integer value) {
        require(value != null && value >= 0 && value <= 100, "百分比必须在零到一百之间");
    }

    private void require(boolean valid, String message) {
        if (!valid) {
            throw new ServiceException(message, 400);
        }
    }
}
