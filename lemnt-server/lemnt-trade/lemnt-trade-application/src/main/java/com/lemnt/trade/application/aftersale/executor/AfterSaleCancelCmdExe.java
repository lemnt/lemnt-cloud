package com.lemnt.trade.application.aftersale.executor;

import com.lemnt.trade.application.aftersale.service.AfterSaleService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 本人取消售后申请。 */
@Component
@RequiredArgsConstructor
public class AfterSaleCancelCmdExe {
    private final AfterSaleService service;
    /** 行锁下校验会员归属和可取消阶段。 */
    public boolean execute(Long memberId, String no) {
        return service.cancel(memberId, no) != null;
    }
}
