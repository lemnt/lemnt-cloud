package com.lemnt.trade.application.brokerage.executor;

import com.lemnt.trade.application.brokerage.dto.BrokerageUserDTO;
import com.lemnt.trade.application.brokerage.service.BrokerageService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 会员自身分销信息查询执行器。 */
@Component
@RequiredArgsConstructor
public class AppBrokerageUserQueryExe {
    private final BrokerageService service;

    public BrokerageUserDTO execute(Long memberId) {
        return service.appUser(memberId);
    }
}
