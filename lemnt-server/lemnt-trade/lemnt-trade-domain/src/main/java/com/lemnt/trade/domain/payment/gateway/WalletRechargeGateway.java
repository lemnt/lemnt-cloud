package com.lemnt.trade.domain.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.payment.model.WalletRecharge;
import com.lemnt.trade.domain.payment.model.WalletRechargeFilter;

import java.time.LocalDateTime;

/** 充值单存储契约。 */
public interface WalletRechargeGateway {
    /** request_no 唯一，重复创建返回原单。 */
    WalletRecharge createIfAbsent(WalletRecharge value);

    WalletRecharge findByNo(String rechargeNo);

    WalletRecharge findByRequestNo(String requestNo);

    /** 锁定充值单，调用方持有事务。 */
    WalletRecharge lockByNo(String rechargeNo);

    /** 覆盖状态与时间字段，调用方已持有行锁。 */
    boolean update(WalletRecharge value);

    PageResult<WalletRecharge> page(WalletRechargeFilter filter);

    /** 套餐是否仍被充值单引用。 */
    boolean packageReferenced(Long packageId);

    /** 数据库当前时间。 */
    LocalDateTime databaseNow();
}
