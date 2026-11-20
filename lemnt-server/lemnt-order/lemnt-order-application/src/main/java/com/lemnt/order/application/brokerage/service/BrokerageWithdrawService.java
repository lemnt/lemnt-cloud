package com.lemnt.order.application.brokerage.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.order.application.brokerage.cmd.BrokerageWithdrawAuditCmd;
import com.lemnt.order.application.brokerage.cmd.BrokerageWithdrawCreateCmd;
import com.lemnt.order.application.brokerage.dto.BrokerageWithdrawDTO;
import com.lemnt.order.application.brokerage.qry.BrokerageWithdrawPageQry;
import com.lemnt.order.domain.brokerage.gateway.BrokerageWalletGateway;
import com.lemnt.order.domain.brokerage.gateway.BrokerageWithdrawGateway;
import com.lemnt.order.domain.brokerage.model.BrokerageWithdraw;
import com.lemnt.order.domain.brokerage.model.BrokerageWithdrawFilter;
import com.lemnt.order.domain.config.gateway.TradeConfigGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 佣金提现：申请扣除可用佣金，审核通过后钱包或手工到账，驳回返还。
 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class BrokerageWithdrawService {
    public static final int STATUS_AUDITING = 0;
    public static final int STATUS_AUDIT_SUCCESS = 10;
    public static final int STATUS_SUCCESS = 11;
    public static final int STATUS_AUDIT_FAIL = 20;
    public static final int TYPE_WALLET = 1;
    public static final int TYPE_BANK = 2;
    public static final int TYPE_WECHAT_QR = 3;
    public static final int TYPE_ALIPAY_QR = 4;

    private final TradeConfigGateway configs;
    private final BrokerageWithdrawGateway withdraws;
    private final BrokerageService brokerage;
    private final BrokerageWalletGateway wallets;
    private final BrokerageWithdrawalStageService stages;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    public Long create(BrokerageWithdrawCreateCmd cmd) {
        require(cmd != null && cmd.memberId() != null && cmd.memberId() > 0, "会员编号不合法");
        var config = configs.get();
        require(config != null && Boolean.TRUE.equals(config.brokerageEnabled()), "分销功能未启用");
        var user = brokerage.ensure(cmd.memberId());
        require(Boolean.TRUE.equals(user.brokerageEnabled()), "尚未开通分销资格");
        require(
            cmd.type() != null
                && config.brokerageWithdrawTypes() != null
                && config.brokerageWithdrawTypes().contains(cmd.type()),
            "提现方式未启用");
        validateFields(cmd);
        BigDecimal price = money(cmd.price());
        BigDecimal min =
            config.brokerageWithdrawMinPrice() == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(config.brokerageWithdrawMinPrice(), 2);
        require(price.compareTo(min) >= 0, "提现金额不能低于最低提现金额");
        require(user.brokeragePrice().compareTo(price) >= 0, "可用佣金不足");
        int feePercent =
            config.brokerageWithdrawFeePercent() == null
                ? 0
                : config.brokerageWithdrawFeePercent();
        BigDecimal fee =
            price.multiply(BigDecimal.valueOf(feePercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
        require(fee.compareTo(price) <= 0, "提现手续费不能超过提现金额");
        boolean wallet = cmd.type() == TYPE_WALLET;
        var saved =
            withdraws.save(
                new BrokerageWithdraw(
                    null,
                    cmd.memberId(),
                    price,
                    fee,
                    user.brokeragePrice(),
                    cmd.type(),
                    wallet ? "本人钱包" : cmd.userName(),
                    wallet ? String.valueOf(cmd.memberId()) : cmd.userAccount(),
                    cmd.qrCodeUrl(),
                    cmd.bankName(),
                    cmd.bankAddress(),
                    STATUS_AUDITING,
                    null,
                    null,
                    null,
                    LocalDateTime.now()));
        brokerage.deductForWithdraw(cmd.memberId(), saved.id(), price);
        return saved.id();
    }

    /**
     * 审批本地提交后才请求钱包，崩溃或超时保留10状态并按原提现号恢复。
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Boolean audit(BrokerageWithdrawAuditCmd cmd) {
        var v = stages.prepare(cmd);
        if (v.status() != STATUS_AUDIT_SUCCESS) return true;
        var net = v.price().subtract(v.feePrice());
        if (v.type() == TYPE_WALLET && net.signum() > 0) wallets.credit(v);
        stages.complete(v.id());
        return true;
    }

    public PageResult<BrokerageWithdrawDTO> page(BrokerageWithdrawPageQry qry) {
        require(
            qry != null
                && qry.pageNum() >= 1
                && qry.pageNum() <= 1000000
                && qry.pageSize() >= 1
                && qry.pageSize() <= 100,
            "分页参数不合法");
        var result =
            withdraws.page(
                new BrokerageWithdrawFilter(
                    qry.userId(), qry.status(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(
            result.getRecords().stream().map(this::dto).toList(), result.getTotal());
    }

    private void validateFields(BrokerageWithdrawCreateCmd cmd) {
        if (cmd.type() == TYPE_WALLET) {
            return;
        }
        require(
            cmd.userName() != null
                && !cmd.userName().isBlank()
                && cmd.userName().length() <= 64,
            "收款人姓名不能为空且不超过64字");
        if (cmd.type() == TYPE_BANK) {
            require(
                cmd.userAccount() != null
                    && !cmd.userAccount().isBlank()
                    && cmd.userAccount().length() <= 128,
                "银行卡号不能为空且不超过128字");
            require(
                cmd.bankName() != null
                    && !cmd.bankName().isBlank()
                    && cmd.bankName().length() <= 100,
                "银行名称不能为空且不超过100字");
            require(
                cmd.bankAddress() != null
                    && !cmd.bankAddress().isBlank()
                    && cmd.bankAddress().length() <= 255,
                "开户地址不能为空且不超过255字");
            return;
        }
        require(
            cmd.qrCodeUrl() != null
                && cmd.qrCodeUrl().length() <= 500
                && (cmd.qrCodeUrl().startsWith("https://")
                || cmd.qrCodeUrl().startsWith("http://")),
            "收款码必须是http或https地址");
    }

    private BigDecimal money(BigDecimal value) {
        require(value != null && value.signum() > 0 && value.scale() <= 2, "提现金额不合法");
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }

    private BrokerageWithdrawDTO dto(BrokerageWithdraw value) {
        return new BrokerageWithdrawDTO(
            value.id(),
            value.userId(),
            value.price(),
            value.feePrice(),
            value.totalPrice(),
            value.type(),
            value.userName(),
            value.userAccount(),
            value.qrCodeUrl(),
            value.bankName(),
            value.bankAddress(),
            value.status(),
            value.auditReason(),
            value.auditTime(),
            value.createTime());
    }
}
