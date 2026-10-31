package com.lemnt.trade.domain.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.payment.model.WalletTransfer;
import com.lemnt.trade.domain.payment.model.WalletTransferFilter;

/** 钱包转账存储契约。 */
public interface WalletTransferGateway {
    /** request_no 唯一，重复创建返回原记录。 */
    WalletTransfer createIfAbsent(WalletTransfer value);

    WalletTransfer findByNo(String transferNo);

    WalletTransfer findByRequestNo(String requestNo);

    PageResult<WalletTransfer> page(WalletTransferFilter filter);
}
