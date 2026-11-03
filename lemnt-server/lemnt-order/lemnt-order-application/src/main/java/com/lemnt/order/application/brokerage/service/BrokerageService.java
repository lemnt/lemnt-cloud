package com.lemnt.order.application.brokerage.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.order.application.brokerage.dto.BrokerageRecordDTO;
import com.lemnt.order.application.brokerage.dto.BrokerageUserDTO;
import com.lemnt.order.application.brokerage.qry.BrokerageRecordPageQry;
import com.lemnt.order.application.brokerage.qry.BrokerageUserPageQry;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.domain.brokerage.gateway.BrokerageMemberGateway;
import com.lemnt.order.domain.brokerage.gateway.BrokerageRecordGateway;
import com.lemnt.order.domain.brokerage.gateway.BrokerageUserGateway;
import com.lemnt.order.domain.brokerage.model.BrokerageRecord;
import com.lemnt.order.domain.brokerage.model.BrokerageRecordFilter;
import com.lemnt.order.domain.brokerage.model.BrokerageUser;
import com.lemnt.order.domain.brokerage.model.BrokerageUserFilter;
import com.lemnt.order.domain.config.gateway.TradeConfigGateway;
import com.lemnt.order.domain.config.model.TradeConfig;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/** 分销关系与佣金：绑定校验、订单佣金、退款取消、到期解冻和提现扣减。 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class BrokerageService {
    public static final int BIZ_ORDER = 1;
    public static final int BIZ_WITHDRAW = 2;
    public static final int BIZ_WITHDRAW_REJECT = 3;
    public static final int BIZ_ORDER_REFUND = 4;
    public static final int STATUS_WAIT = 0;
    public static final int STATUS_SETTLED = 1;
    public static final int STATUS_CANCEL = 2;
    public static final int ENABLED_ALL = 1;
    public static final int BIND_OVERRIDE = 3;
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final TradeConfigGateway configs;
    private final BrokerageUserGateway users;
    private final BrokerageRecordGateway records;
    private final BrokerageMemberGateway members;
    private final OrderPersistenceService orders;
    private final BrokerageSettlementService settlement;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    private TradeConfig config() {
        var value = configs.get();
        require(value != null && Boolean.TRUE.equals(value.brokerageEnabled()), "分销功能未启用");
        return value;
    }

    /** 人人分销时自动开通；指定分销时保留未开通状态，但仍保存绑定关系。 */
    public BrokerageUser ensure(Long memberId) {
        require(memberId != null && memberId > 0, "会员编号不合法");
        var value = users.find(memberId);
        if (value != null) {
            return value;
        }
        var config = configs.get();
        boolean enabled =
                config != null
                        && Boolean.TRUE.equals(config.brokerageEnabled())
                        && Integer.valueOf(ENABLED_ALL).equals(config.brokerageEnabledCondition());
        return users.save(
                new BrokerageUser(
                        memberId,
                        null,
                        null,
                        enabled,
                        enabled ? LocalDateTime.now() : null,
                        ZERO,
                        ZERO));
    }

    public BrokerageUserDTO appUser(Long memberId) {
        return dto(ensure(memberId));
    }

    public Boolean bind(Long memberId, Long bindUserId) {
        var config = config();
        require(bindUserId != null && bindUserId > 0 && !bindUserId.equals(memberId), "不能绑定自己为推广员");
        var user = ensure(memberId);
        if (user.bindUserId() != null) {
            if (user.bindUserId().equals(bindUserId)) {
                return true;
            }
            require(
                    Integer.valueOf(BIND_OVERRIDE).equals(config.brokerageBindMode()),
                    "已绑定推广员，不能重复绑定");
        }
        var target = users.find(bindUserId);
        require(target != null && Boolean.TRUE.equals(target.brokerageEnabled()), "推广员不存在或未开通分销资格");
        validateNoCycle(memberId, target);
        users.save(
                new BrokerageUser(
                        user.id(),
                        bindUserId,
                        LocalDateTime.now(),
                        user.brokerageEnabled(),
                        user.brokerageTime(),
                        user.brokeragePrice(),
                        user.frozenPrice()));
        return true;
    }

    /** 管理端修改推广员，不受绑定模式限制，但仍禁止自绑和环。 */
    public Boolean adminBind(Long memberId, Long bindUserId) {
        var user = ensure(memberId);
        if (bindUserId == null) {
            require(users.clearBind(user.id()), "清除推广员失败");
            return true;
        }
        require(!bindUserId.equals(memberId), "不能绑定自己为推广员");
        var target = users.find(bindUserId);
        require(target != null && Boolean.TRUE.equals(target.brokerageEnabled()), "推广员不存在或未开通分销资格");
        validateNoCycle(memberId, target);
        users.save(
                new BrokerageUser(
                        user.id(),
                        bindUserId,
                        LocalDateTime.now(),
                        user.brokerageEnabled(),
                        user.brokerageTime(),
                        user.brokeragePrice(),
                        user.frozenPrice()));
        return true;
    }

    private void validateNoCycle(Long memberId, BrokerageUser target) {
        Long cursor = target.bindUserId();
        for (int depth = 0; cursor != null && depth < 10; depth++) {
            require(!cursor.equals(memberId), "推广关系不能形成环");
            var next = users.find(cursor);
            cursor = next == null ? null : next.bindUserId();
        }
    }

    public Boolean setEnabled(Long memberId, Boolean enabled) {
        require(enabled != null, "分销资格状态不能为空");
        var user = ensure(memberId);
        users.save(
                new BrokerageUser(
                        user.id(),
                        user.bindUserId(),
                        user.bindUserTime(),
                        enabled,
                        enabled ? LocalDateTime.now() : null,
                        user.brokeragePrice(),
                        user.frozenPrice()));
        return true;
    }

    public PageResult<BrokerageUserDTO> pageUsers(BrokerageUserPageQry qry) {
        page(qry == null ? 0 : qry.pageNum(), qry == null ? 0 : qry.pageSize());
        var result =
                users.page(
                        new BrokerageUserFilter(
                                qry.bindUserId(),
                                qry.brokerageEnabled(),
                                qry.pageNum(),
                                qry.pageSize()));
        return PageResult.of(
                result.getRecords().stream().map(this::dto).toList(), result.getTotal());
    }

    public PageResult<BrokerageRecordDTO> pageRecords(BrokerageRecordPageQry qry) {
        page(qry == null ? 0 : qry.pageNum(), qry == null ? 0 : qry.pageSize());
        require(
                qry.bizType() == null
                        || List.of(BIZ_ORDER, BIZ_WITHDRAW, BIZ_WITHDRAW_REJECT, BIZ_ORDER_REFUND)
                                .contains(qry.bizType()),
                "佣金业务类型不合法");
        require(
                qry.status() == null
                        || List.of(STATUS_WAIT, STATUS_SETTLED, STATUS_CANCEL)
                                .contains(qry.status()),
                "佣金状态不合法");
        var result =
                records.page(
                        new BrokerageRecordFilter(
                                qry.userId(),
                                qry.bizType(),
                                qry.status(),
                                qry.pageNum(),
                                qry.pageSize()));
        return PageResult.of(
                result.getRecords().stream().map(this::recordDto).toList(), result.getTotal());
    }

    /** 订单支付后按一级、二级推广人计算佣金，同一订单同一推广人只记一次。 */
    public int addOrderCommission(String orderNo) {
        var config = configs.get();
        if (config == null || !Boolean.TRUE.equals(config.brokerageEnabled())) {
            return 0;
        }
        var order = orders.findByOrderNo(orderNo);
        if (order == null || order.totalAmount() == null || order.totalAmount().signum() <= 0) {
            return 0;
        }
        var buyer = users.find(order.memberId());
        if (buyer == null || buyer.bindUserId() == null) {
            return 0;
        }
        var first = users.find(buyer.bindUserId());
        if (first == null || !Boolean.TRUE.equals(first.brokerageEnabled())) {
            return 0;
        }
        int count =
                credit(
                        first,
                        order.orderNo(),
                        order.totalAmount(),
                        config.brokerageFirstPercent(),
                        config.brokerageFrozenDays(),
                        1,
                        order.memberId(),
                        "获得推广佣金");
        if (first.bindUserId() == null) {
            return count;
        }
        var second = users.find(first.bindUserId());
        if (second != null && Boolean.TRUE.equals(second.brokerageEnabled())) {
            count +=
                    credit(
                            second,
                            order.orderNo(),
                            order.totalAmount(),
                            config.brokerageSecondPercent(),
                            config.brokerageFrozenDays(),
                            2,
                            order.memberId(),
                            "获得二级推广佣金");
        }
        return count;
    }

    private int credit(
            BrokerageUser user,
            String orderNo,
            BigDecimal amount,
            Integer percent,
            Integer frozenDays,
            int level,
            Long sourceUserId,
            String title) {
        if (percent == null || percent <= 0) {
            return 0;
        }
        BigDecimal price =
                amount.multiply(BigDecimal.valueOf(percent))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
        if (price.signum() <= 0 || records.findByBiz(user.id(), BIZ_ORDER, orderNo) != null) {
            return 0;
        }
        int days = frozenDays == null ? 0 : Math.max(frozenDays, 0);
        LocalDateTime now = LocalDateTime.now();
        var locked = users.lock(user.id());
        require(locked != null, "分销用户不存在");
        BigDecimal frozen = locked.frozenPrice();
        BigDecimal available = locked.brokeragePrice();
        if (days > 0) {
            frozen = frozen.add(price);
        } else {
            available = available.add(price);
        }
        users.save(
                new BrokerageUser(
                        locked.id(),
                        locked.bindUserId(),
                        locked.bindUserTime(),
                        locked.brokerageEnabled(),
                        locked.brokerageTime(),
                        available,
                        frozen));
        records.save(
                new BrokerageRecord(
                        null,
                        user.id(),
                        orderNo,
                        BIZ_ORDER,
                        title,
                        orderNo,
                        price,
                        available.add(frozen),
                        days > 0 ? STATUS_WAIT : STATUS_SETTLED,
                        days,
                        days > 0 ? now.plusDays(days) : null,
                        level,
                        sourceUserId,
                        now));
        return 1;
    }

    /** 整单退款后取消该订单佣金：扣回尚未因部分退款扣减的剩余佣金。 */
    public int cancelOrderCommission(String orderNo) {
        int count = 0;
        for (var record : records.byBiz(BIZ_ORDER, orderNo)) {
            if (record.status() == STATUS_CANCEL
                    || !records.updateStatus(record.id(), record.status(), STATUS_CANCEL)) {
                continue;
            }
            var user = users.lock(record.userId());
            require(user != null, "分销用户不存在");
            var reversal = records.findByBiz(record.userId(), BIZ_ORDER_REFUND, orderNo);
            BigDecimal recalled = reversal == null ? ZERO : reversal.price().negate();
            BigDecimal remaining = record.price().subtract(recalled).max(ZERO);
            var deducted = deduct(user, record.status() == STATUS_WAIT, remaining);
            if (deducted.remainder().signum() != 0) {
                log.warn(
                        "整单退款佣金余额不足, orderNo={}, userId={}, missing={}",
                        orderNo,
                        record.userId(),
                        deducted.remainder());
            }
            count++;
        }
        return count;
    }

    /** 部分退款按累计成功退款金额比例扣回已发佣金，重复回调只补差额。 */
    public int recallOrderCommission(
            String orderNo, BigDecimal refundedAmount, BigDecimal orderTotal) {
        if (orderNo == null
                || refundedAmount == null
                || orderTotal == null
                || refundedAmount.signum() <= 0
                || orderTotal.signum() <= 0) {
            return 0;
        }
        BigDecimal ratioBase = refundedAmount.min(orderTotal);
        int count = 0;
        for (var record : records.byBiz(BIZ_ORDER, orderNo)) {
            if (record.status() == STATUS_CANCEL) {
                continue;
            }
            BigDecimal expected =
                    record.price()
                            .multiply(ratioBase)
                            .divide(orderTotal, 2, RoundingMode.DOWN)
                            .min(record.price());
            var reversal = records.findByBiz(record.userId(), BIZ_ORDER_REFUND, orderNo);
            BigDecimal recalled = reversal == null ? ZERO : reversal.price().negate();
            BigDecimal delta = expected.subtract(recalled);
            if (delta.signum() <= 0) {
                continue;
            }
            var user = users.lock(record.userId());
            require(user != null, "分销用户不存在");
            var deducted = deduct(user, record.status() == STATUS_WAIT, delta);
            if (deducted.remainder().signum() != 0) {
                log.warn(
                        "部分退款佣金余额不足, orderNo={}, userId={}, missing={}",
                        orderNo,
                        record.userId(),
                        deducted.remainder());
            }
            if (deducted.actual().signum() <= 0) {
                continue;
            }
            BigDecimal next = recalled.add(deducted.actual());
            var target = deducted.user();
            if (reversal == null) {
                records.save(
                        new BrokerageRecord(
                                null,
                                record.userId(),
                                orderNo,
                                BIZ_ORDER_REFUND,
                                "退款扣回佣金",
                                "订单退款累计扣回 " + next.toPlainString(),
                                next.negate(),
                                target.brokeragePrice().add(target.frozenPrice()),
                                STATUS_SETTLED,
                                0,
                                null,
                                record.sourceUserLevel(),
                                record.sourceUserId(),
                                LocalDateTime.now()));
            } else if (!records.updatePrice(reversal.id(), reversal.price(), next.negate())) {
                throw new IllegalStateException("佣金扣回累计更新失败");
            }
            count++;
        }
        return count;
    }

    private record Deduction(BrokerageUser user, BigDecimal actual, BigDecimal remainder) {}

    /** 待结算佣金优先扣冻结，已结算扣可用；余额不足时只扣实际可用部分并返回差额。 */
    private Deduction deduct(BrokerageUser user, boolean fromFrozen, BigDecimal amount) {
        BigDecimal frozen = user.frozenPrice();
        BigDecimal available = user.brokeragePrice();
        BigDecimal remaining = amount.max(ZERO);
        if (fromFrozen) {
            BigDecimal value = remaining.min(frozen);
            frozen = frozen.subtract(value);
            remaining = remaining.subtract(value);
        }
        BigDecimal value = remaining.min(available);
        available = available.subtract(value);
        remaining = remaining.subtract(value);
        users.save(
                new BrokerageUser(
                        user.id(),
                        user.bindUserId(),
                        user.bindUserTime(),
                        user.brokerageEnabled(),
                        user.brokerageTime(),
                        available,
                        frozen));
        return new Deduction(user, amount.subtract(remaining), remaining);
    }

    /** 提现申请成功后扣减可用佣金，并写负值佣金记录。 */
    public void deductForWithdraw(Long userId, Long withdrawId, BigDecimal price) {
        var user = users.lock(userId);
        require(user != null && Boolean.TRUE.equals(user.brokerageEnabled()), "分销用户不存在或未开通");
        require(user.brokeragePrice().compareTo(price) >= 0, "可用佣金不足");
        BigDecimal available = user.brokeragePrice().subtract(price);
        users.save(
                new BrokerageUser(
                        user.id(),
                        user.bindUserId(),
                        user.bindUserTime(),
                        user.brokerageEnabled(),
                        user.brokerageTime(),
                        available,
                        user.frozenPrice()));
        records.save(
                new BrokerageRecord(
                        null,
                        userId,
                        String.valueOf(withdrawId),
                        BIZ_WITHDRAW,
                        "提现申请",
                        "提现申请扣除佣金 " + price.toPlainString(),
                        price.negate(),
                        available.add(user.frozenPrice()),
                        STATUS_SETTLED,
                        0,
                        null,
                        null,
                        null,
                        LocalDateTime.now()));
    }

    /** 提现驳回返还可用佣金。 */
    public void returnRejectedWithdraw(Long userId, Long withdrawId, BigDecimal price) {
        var user = users.lock(userId);
        require(user != null, "分销用户不存在");
        BigDecimal available = user.brokeragePrice().add(price);
        users.save(
                new BrokerageUser(
                        user.id(),
                        user.bindUserId(),
                        user.bindUserTime(),
                        user.brokerageEnabled(),
                        user.brokerageTime(),
                        available,
                        user.frozenPrice()));
        records.save(
                new BrokerageRecord(
                        null,
                        userId,
                        String.valueOf(withdrawId),
                        BIZ_WITHDRAW_REJECT,
                        "提现申请驳回",
                        "提现申请驳回，返还佣金 " + price.toPlainString(),
                        price,
                        available.add(user.frozenPrice()),
                        STATUS_SETTLED,
                        0,
                        null,
                        null,
                        null,
                        LocalDateTime.now()));
    }

    /** 解冻到期佣金，单条失败不影响其余记录。 */
    public int unfreezeDue(int limit) {
        int processed = 0;
        for (var record : records.due(STATUS_WAIT, LocalDateTime.now(), Math.max(limit, 1))) {
            try {
                if (settlement.unfreeze(record.id())) {
                    processed++;
                }
            } catch (RuntimeException ignored) {
                // 单条异常保留待结算状态，下次任务继续重试。
            }
        }
        return processed;
    }

    private BrokerageUserDTO dto(BrokerageUser value) {
        var bind = value.bindUserId() == null ? null : members.find(value.bindUserId());
        long first = users.countChildren(value.id());
        long second = 0;
        for (Long child : users.childIds(value.id())) {
            second += users.countChildren(child);
        }
        return new BrokerageUserDTO(
                value.id(),
                value.bindUserId(),
                bind == null ? null : bind.nickname(),
                bind == null ? null : bind.avatar(),
                value.brokerageEnabled(),
                value.brokeragePrice(),
                value.frozenPrice(),
                first,
                second);
    }

    private BrokerageRecordDTO recordDto(BrokerageRecord value) {
        return new BrokerageRecordDTO(
                value.id(),
                value.userId(),
                value.bizId(),
                value.bizType(),
                value.title(),
                value.description(),
                value.price(),
                value.totalPrice(),
                value.status(),
                value.frozenDays(),
                value.unfreezeTime(),
                value.sourceUserLevel(),
                value.createTime());
    }

    private void page(int pageNum, int pageSize) {
        require(pageNum >= 1 && pageNum <= 1000000 && pageSize >= 1 && pageSize <= 100, "分页参数不合法");
    }
}
