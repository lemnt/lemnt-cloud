package com.lemnt.pay.client;

import com.lemnt.pay.client.bo.BrokerageWithdrawalBo;
import com.lemnt.pay.client.dto.WalletDTO;

/** 钱包账户快照契约；资金写入仍按原业务编号及账本规则执行。 */
public interface WalletClient {
    void creditBrokerageWithdrawal(BrokerageWithdrawalBo input);

    WalletDTO getOrCreate(Long memberId);
}
