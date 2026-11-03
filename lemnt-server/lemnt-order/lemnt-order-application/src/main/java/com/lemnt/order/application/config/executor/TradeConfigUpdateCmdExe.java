package com.lemnt.order.application.config.executor;

import com.lemnt.order.application.config.cmd.TradeConfigUpdateCmd;
import com.lemnt.order.application.config.service.TradeConfigService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 交易配置修改执行器。 */
@Component
@RequiredArgsConstructor
public class TradeConfigUpdateCmdExe {
    private final TradeConfigService service;

    /** 执行交易配置操作。 */
    public boolean execute(TradeConfigUpdateCmd cmd) {
        return service.update(cmd);
    }
}
