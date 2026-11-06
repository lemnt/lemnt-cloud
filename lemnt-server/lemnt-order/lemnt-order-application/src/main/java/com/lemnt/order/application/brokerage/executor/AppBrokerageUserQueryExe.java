package com.lemnt.order.application.brokerage.executor;

import com.lemnt.order.application.brokerage.dto.BrokerageUserDTO;
import com.lemnt.order.application.brokerage.service.BrokerageService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 会员自身分销信息查询执行器。
 */
@Component
@RequiredArgsConstructor
public class AppBrokerageUserQueryExe {
    private final BrokerageService service;

    public BrokerageUserDTO execute(Long memberId) {
        return service.appUser(memberId);
    }
}
