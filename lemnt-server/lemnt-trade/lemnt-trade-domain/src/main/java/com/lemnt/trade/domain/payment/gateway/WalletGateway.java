package com.lemnt.trade.domain.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.payment.model.Wallet;
import com.lemnt.trade.domain.payment.model.WalletFilter;
import com.lemnt.trade.domain.payment.model.WalletTransaction;
import com.lemnt.trade.domain.payment.model.WalletTransactionFilter;

/** 钱包账户与流水存储契约。 */
public interface WalletGateway {
    /** 按会员读取钱包。 */
    Wallet findByMemberId(Long memberId);

    /** 按会员锁定钱包，调用方持有事务。 */
    Wallet lockByMemberId(Long memberId);

    /** 按钱包编号锁定，供转账按编号升序同时锁定双方钱包。 */
    Wallet lockById(Long id);

    /** 会员不存在钱包时创建零余额账户，并发由唯一键兜底。 */
    Wallet createIfAbsent(Long memberId);

    /** 覆盖余额与累计字段，调用方已持有行锁。 */
    boolean update(Wallet wallet);

    /** 分页查询钱包。 */
    PageResult<Wallet> page(WalletFilter filter);

    /** 按业务唯一键读取流水，用于幂等。 */
    WalletTransaction findTransaction(Long walletId, String bizType, String bizNo);

    /** 追加流水，唯一键保证同一业务只记一次。 */
    void insertTransaction(WalletTransaction transaction);

    /** 分页查询钱包流水。 */
    PageResult<WalletTransaction> transactionPage(WalletTransactionFilter filter);
}
