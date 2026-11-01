package com.lemnt.trade.infrastructure.brokerage.converter;

import com.lemnt.trade.domain.brokerage.model.BrokerageRecord;
import com.lemnt.trade.domain.brokerage.model.BrokerageUser;
import com.lemnt.trade.domain.brokerage.model.BrokerageWithdraw;
import com.lemnt.trade.infrastructure.brokerage.dataobject.BrokerageRecordDO;
import com.lemnt.trade.infrastructure.brokerage.dataobject.BrokerageUserDO;
import com.lemnt.trade.infrastructure.brokerage.dataobject.BrokerageWithdrawDO;

/** 分销领域对象与表结构转换。 */
public final class BrokerageConverter {
    private BrokerageConverter() {
    }

    public static BrokerageUser user(BrokerageUserDO row) {
        if (row == null) {
            return null;
        }
        return new BrokerageUser(row.getId(), row.getBindUserId(), row.getBindUserTime(),
                row.getBrokerageEnabled(), row.getBrokerageTime(), row.getBrokeragePrice(),
                row.getFrozenPrice());
    }

    public static BrokerageUserDO row(BrokerageUser value) {
        var row = new BrokerageUserDO();
        row.setId(value.id());
        row.setBindUserId(value.bindUserId());
        row.setBindUserTime(value.bindUserTime());
        row.setBrokerageEnabled(value.brokerageEnabled());
        row.setBrokerageTime(value.brokerageTime());
        row.setBrokeragePrice(value.brokeragePrice());
        row.setFrozenPrice(value.frozenPrice());
        return row;
    }

    public static BrokerageRecord record(BrokerageRecordDO row) {
        if (row == null) {
            return null;
        }
        return new BrokerageRecord(row.getId(), row.getUserId(), row.getBizId(), row.getBizType(),
                row.getTitle(), row.getDescription(), row.getPrice(), row.getTotalPrice(), row.getStatus(),
                row.getFrozenDays(), row.getUnfreezeTime(), row.getSourceUserLevel(), row.getSourceUserId(),
                row.getCreateTime());
    }

    public static BrokerageRecordDO row(BrokerageRecord value) {
        var row = new BrokerageRecordDO();
        row.setId(value.id());
        row.setUserId(value.userId());
        row.setBizId(value.bizId());
        row.setBizType(value.bizType());
        row.setTitle(value.title());
        row.setDescription(value.description());
        row.setPrice(value.price());
        row.setTotalPrice(value.totalPrice());
        row.setStatus(value.status());
        row.setFrozenDays(value.frozenDays());
        row.setUnfreezeTime(value.unfreezeTime());
        row.setSourceUserLevel(value.sourceUserLevel());
        row.setSourceUserId(value.sourceUserId());
        row.setCreateTime(value.createTime());
        return row;
    }

    public static BrokerageWithdraw withdraw(BrokerageWithdrawDO row) {
        if (row == null) {
            return null;
        }
        return new BrokerageWithdraw(row.getId(), row.getUserId(), row.getPrice(), row.getFeePrice(),
                row.getTotalPrice(), row.getType(), row.getUserName(), row.getUserAccount(),
                row.getQrCodeUrl(), row.getBankName(), row.getBankAddress(), row.getStatus(),
                row.getAuditReason(), row.getAuditTime(), row.getRemark(), row.getCreateTime());
    }

    public static BrokerageWithdrawDO row(BrokerageWithdraw value) {
        var row = new BrokerageWithdrawDO();
        row.setId(value.id());
        row.setUserId(value.userId());
        row.setPrice(value.price());
        row.setFeePrice(value.feePrice());
        row.setTotalPrice(value.totalPrice());
        row.setType(value.type());
        row.setUserName(value.userName());
        row.setUserAccount(value.userAccount());
        row.setQrCodeUrl(value.qrCodeUrl());
        row.setBankName(value.bankName());
        row.setBankAddress(value.bankAddress());
        row.setStatus(value.status());
        row.setAuditReason(value.auditReason());
        row.setAuditTime(value.auditTime());
        row.setRemark(value.remark());
        row.setCreateTime(value.createTime());
        return row;
    }
}
