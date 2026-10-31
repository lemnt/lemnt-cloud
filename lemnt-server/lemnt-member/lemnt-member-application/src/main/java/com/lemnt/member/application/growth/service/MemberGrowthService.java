package com.lemnt.member.application.growth.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.growth.gateway.MemberPointRecordGateway;
import com.lemnt.member.domain.growth.gateway.MemberExperienceRecordGateway;
import com.lemnt.member.domain.growth.gateway.MemberConfigGateway;
import com.lemnt.member.domain.growth.gateway.MemberConfigLockGateway;
import com.lemnt.member.domain.growth.gateway.MemberLevelGateway;
import com.lemnt.member.domain.growth.gateway.MemberLevelRecordGateway;
import com.lemnt.member.domain.growth.gateway.MemberSignConfigGateway;
import com.lemnt.member.domain.growth.gateway.MemberSignRecordGateway;
import com.lemnt.member.domain.growth.model.AssetRecord;
import com.lemnt.member.domain.growth.model.Config;
import com.lemnt.member.domain.growth.model.Level;
import com.lemnt.member.domain.growth.model.LevelRecord;
import com.lemnt.member.domain.growth.model.SignConfig;
import com.lemnt.member.domain.growth.model.SignRecord;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberUserGateway;
import com.lemnt.member.domain.user.model.Member;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

/** 会员成长权益应用编排，保证签到、资产流水及等级变更原子性。 */
@Component
@RequiredArgsConstructor
public class MemberGrowthService {

    private final MemberPointRecordGateway pointRecords;
    private final MemberExperienceRecordGateway experienceRecords;
    private final MemberConfigGateway memberConfigGateway;
    private final MemberConfigLockGateway configLocks;
    private final MemberLevelGateway memberLevelGateway;
    private final MemberLevelRecordGateway memberLevelRecordGateway;
    private final MemberSignConfigGateway memberSignConfigGateway;
    private final MemberSignRecordGateway memberSignRecordGateway;
    private final MemberUserGateway memberUserGateway;

    /** 应用编排直接选择逐表网关，不保留泛化领域存储接口。 */
    private AssetRecord assetRequest(Long memberId,String type,String code,String bizNo) {
        return switch(type) {
            case "POINT" -> pointRecords.request(memberId,code,bizNo);
            case "EXPERIENCE" -> experienceRecords.request(memberId,code,bizNo);
            default -> throw new ServiceException("资产类型不合法",400);
        };
    }
    /** 已持会员锁，写入对应账本并参加外层事务。 */
    private void saveAsset(AssetRecord record) {
        switch(record.assetType()) {
            case "POINT" -> pointRecords.save(record);
            case "EXPERIENCE" -> experienceRecords.save(record);
            default -> throw new ServiceException("资产类型不合法",400);
        }
    }
    /** 退款累计只查询原权益所属账本。 */
    private long tradeReturns(Long memberId,String type,String code,String orderNo) {
        return switch(type) {
            case "POINT" -> pointRecords.returned(memberId,code,orderNo);
            case "EXPERIENCE" -> experienceRecords.returned(memberId,code,orderNo);
            default -> throw new ServiceException("资产类型不合法",400);
        };
    }

    private Member lock(Long id) {
        MemberRules.id(id);
        var value = memberUserGateway.lockById(id);
        if (value == null) {
            throw new ServiceException("会员不存在", 400);
        }
        return value;
    }

    private Member active(Long id) {
        var value = lock(id);
        if (!Integer.valueOf(1).equals(value.status())) {
            throw new ServiceException("会员已停用", 401);
        }
        return value;
    }

    /**
     * 在会员行锁内核对业务幂等键，更新余额、流水和自动升级记录。
     * 由调整、等级分配或签到的外层事务统一提交，奖励失败时一并回滚。
     */
    private AssetRecord adjust(
            Long memberId,
            String type,
            long delta,
            String bizType,
            String key,
            String reason,
            boolean allowZero,
            boolean autoLevel) {
        var user = lock(memberId);
        if (!Set.of("POINT", "EXPERIENCE").contains(type)) {
            throw new ServiceException("资产类型不合法", 400);
        }
        bizType = MemberRules.text(bizType, 40);
        key = MemberRules.text(key, 64);
        reason = MemberRules.text(reason, 500);
        var previous = assetRequest(memberId, type, bizType, key);
        if (previous != null) {
            if (previous.delta() != delta || !previous.reason().equals(reason)) {
                throw new ServiceException("业务编号已用于不同内容", 409);
            }
            return previous;
        }
        if (delta == 0 && !allowZero) {
            throw new ServiceException("变更量不能为0", 400);
        }
        long balance =
                Math.addExact("POINT".equals(type) ? user.points() : user.experience(), delta);
        // 经验按源规则扣至零，流水仍保留请求变更量；积分继续校验余额。
        if ("EXPERIENCE".equals(type)) {
            balance = Math.max(balance, 0);
        } else if (balance < 0) {
            throw new ServiceException("余额不足", 400);
        }
        memberUserGateway.balance(memberId, type, balance);
        var metadata=com.lemnt.member.domain.growth.model.PointRecordMetadata.from(type,bizType,delta);
        var expMetadata=com.lemnt.member.domain.growth.model.ExperienceRecordMetadata.from(type,bizType,delta);
        var record =
                new AssetRecord(
                        null,
                        memberId,
                        type,
                        delta,
                        balance,
                        bizType,
                        key,
                        reason,
                        memberUserGateway.now(),null,metadata.type(),"POINT".equals(type)?metadata.title():expMetadata.title(),"POINT".equals(type)?metadata.description():expMetadata.description(),expMetadata.type()
                );
        saveAsset(record);
        if (autoLevel && "EXPERIENCE".equals(type)) {
            long userExperience = balance;
            var best =
                    memberLevelGateway.levels().stream()
                    .filter(v -> v.status() == 1 && v.experience() <= userExperience)
                    .max(Comparator.comparing(Level::level))
                    .orElse(null);
            // 无匹配的启用等级时保留原等级，仅明确匹配到不同等级才变更。
            if (best != null && !Objects.equals(best.id(), user.levelId())) {
                memberUserGateway.assignLevel(memberId, best.id());
                memberLevelRecordGateway.levelRecord(
                        new LevelRecord(
                                null,
                                memberId,
                                best.id(),
                                balance,
                                reason,
                                memberUserGateway.now(),
                                best.level(),
                                best.discountPercent(),
                                best.experience(),
                                null));
            }
        }
        return assetRequest(memberId, type, bizType, key);
    }

    /**
     * 调整资产。
     */
    @Transactional(rollbackFor = Exception.class)
    public AssetRecord adjustAsset(
            Long memberId, String type, long delta, String requestNo, String reason) {
        return adjust(memberId, type, delta, "ADMIN", requestNo, reason, false, true);
    }

    /**
     * 分配等级。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean assignLevel(Long memberId, Long levelId, String requestNo, String reason) {
        var user = lock(memberId);
        String key = MemberRules.text(requestNo, 64);
        String audit =
                MemberRules.text("[level=" + levelId + "] " + MemberRules.text(reason, 450), 500);
        var previous = assetRequest(memberId, "EXPERIENCE", "ADMIN_LEVEL", key);
        if (previous != null) {
            if (!audit.equals(previous.reason())) {
                throw new ServiceException("业务编号已用于不同等级操作", 409);
            }
            return true;
        }
        // 与源规则一致：等级未变化时保留经验，不生成经验或等级流水。
        if (Objects.equals(user.levelId(), levelId)) {
            return true;
        }
        Level level = levelId == null ? null : memberLevelGateway.level(MemberRules.id(levelId));
        if (levelId != null && (level == null || level.status() != 1)) {
            throw new ServiceException("等级不存在或已停用", 400);
        }
        long target = level == null ? 0 : level.experience();
        adjust(
                memberId,
                "EXPERIENCE",
                target - user.experience(),
                "ADMIN_LEVEL",
                key,
                audit,
                true,
                false);
        memberUserGateway.assignLevel(memberId, levelId);
        memberLevelRecordGateway.levelRecord(
                new LevelRecord(
                        null,
                        memberId,
                        levelId,
                        target,
                        audit,
                        memberUserGateway.now(),
                        level == null ? null : level.level(),
                        level == null ? null : level.discountPercent(),
                        target - user.experience(),
                        level == null ? "管理员取消了等级" : "管理员调整为：" + level.name()));
        return true;
    }

    /**
     * 保存等级。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveLevel(Level v) {
        configLocks.lockConfiguration();
        if (v.id() != null && memberLevelGateway.level(MemberRules.id(v.id())) == null) {
            throw new ServiceException("等级不存在", 400);
        }
        String name = MemberRules.text(v.name(), 80);
        if (v.level() == null
                || v.level() < 1
                || v.experience() == null
                || v.experience() < 1
                || v.discountPercent() == null
                || v.discountPercent() < 0
                || v.discountPercent() > 100
                || v.status() == null
                || (v.status() != 0 && v.status() != 1)) {
            throw new ServiceException("等级参数不合法", 400);
        }
        for (var old : memberLevelGateway.levels()) {
            if (!Objects.equals(old.id(), v.id())) {
                if (old.name().equals(name)
                        || old.level().equals(v.level())
                        || (old.level() < v.level() && old.experience() >= v.experience())
                        || (old.level() > v.level() && old.experience() <= v.experience())) {
                    throw new ServiceException("等级名称、序号或经验门槛冲突", 400);
                }
            }
        }
        return memberLevelGateway.levelSave(
                new Level(
                        v.id(),
                        name,
                        v.level(),
                        v.experience(),
                        v.discountPercent(),
                        MemberRules.optional(v.icon(), 500),
                        MemberRules.optional(v.backgroundUrl(), 500),
                        v.status()));
    }

    /**
     * 删除等级。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteLevel(Long id) {
        configLocks.lockConfiguration();
        MemberRules.id(id);
        if (memberLevelGateway.level(id) == null) {
            throw new ServiceException("等级不存在", 404);
        }
        if (!memberLevelGateway.levelDelete(id)) {
            throw new ServiceException("等级下有会员，无法删除", 409);
        }
        return true;
    }

    /** 创建规则，天数重复不能覆盖原奖励。 */
    @Transactional(rollbackFor = Exception.class)
    public Long createSignConfig(SignConfig value) {
        validateSignConfig(value);
        if (memberSignConfigGateway.signConfigByDay(value.day()) != null) {
            throw new ServiceException("签到天数已存在", 409);
        }
        return memberSignConfigGateway.create(new SignConfig(value.day(), value.point(), value.experience(), value.status()));
    }

    /** 修改规则可调整天数，但不能占用其他规则的天数。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSignConfig(SignConfig value) {
        MemberRules.id(value.id());
        validateSignConfig(value);
        if (memberSignConfigGateway.signConfig(value.id()) == null) {
            throw new ServiceException("签到规则不存在", 404);
        }
        var existing = memberSignConfigGateway.signConfigByDay(value.day());
        if (existing != null && !existing.id().equals(value.id())) {
            throw new ServiceException("签到天数已存在", 409);
        }
        if (!memberSignConfigGateway.update(value)) {
            throw new ServiceException("签到规则不存在", 404);
        }
        return true;
    }

    /** 校验规则内容，保留现有天数及奖励约束。 */
    private void validateSignConfig(SignConfig v) {
        if (v.day() == null || v.day() < 1 || v.day() > 366
                || v.point() == null || v.point() < 0
                || v.experience() == null || v.experience() < 0
                || (v.point() == 0 && v.experience() == 0)
                || v.status() == null || (v.status() != 0 && v.status() != 1)) {
            throw new ServiceException("签到配置不合法", 400);
        }
    }

    /** 按ID删除，不影响已发放的签到奖励。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSignConfig(Long id) {
        MemberRules.id(id);
        if (!memberSignConfigGateway.signConfigDelete(id)) {
            throw new ServiceException("签到规则不存在", 404);
        }
        return true;
    }

    /**
     * 签到。
     */
    @Transactional(rollbackFor = Exception.class)
    public SignRecord sign(Long memberId) {
        active(memberId);
        // 与签到表DATETIME秒精度一致，避免数据库舍入事件时间跨秒。
        var now = memberUserGateway.now().withNano(0);
        var today = now.toLocalDate();
        var previous = memberSignRecordGateway.signDate(memberId, today);
        if (previous != null) {
            throw new ServiceException("今日已签到", 409);
        }
        var configs = memberSignConfigGateway.signConfigs().stream().filter(v -> v.status() == 1).toList();
        if (configs.isEmpty()) {
            throw new ServiceException("签到规则未启用", 409);
        }
        int max = configs.stream().mapToInt(SignConfig::day).max().orElse(1);
        var last = memberSignRecordGateway.lastSign(memberId);
        int day = last != null && last.signDate().equals(today.minusDays(1)) ? last.day() + 1 : 1;
        if (day > max) {
            day = 1;
        }
        final int current = day;
        var config =
                configs.stream()
                .filter(v -> v.day() == current)
                .findFirst()
                .orElse(new SignConfig(day, 0L, 0L, 1));
        var signRecordId = memberSignRecordGateway.signSave(
                new SignRecord(null, memberId, today, day, config.point(), config.experience(), null, now));
        if (config.point() > 0) {
            adjust(
                    memberId,
                    "POINT",
                    config.point(),
                    "SIGN_IN",
                    signRecordId.toString(),
                    "每日签到",
                    false,
                    true);
        }
        if (config.experience() > 0) {
            adjust(
                    memberId,
                    "EXPERIENCE",
                    config.experience(),
                    "SIGN_IN",
                    signRecordId.toString(),
                    "每日签到",
                    false,
                    true);
        }
        return memberSignRecordGateway.signDate(memberId, today);
    }

    /**
     * 保存配置。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveConfig(Config v) {
        configLocks.lockConfiguration();
        if (v.pointTradeDeductEnable() == null
                || v.pointTradeDeductUnitPrice() == null
                || v.pointTradeDeductUnitPrice() < 0
                || v.pointTradeDeductMaxPrice() == null
                || v.pointTradeDeductMaxPrice() < 0
                || v.pointTradeGivePoint() == null
                || v.pointTradeGivePoint() < 0
                || (v.pointTradeDeductEnable() && v.pointTradeDeductUnitPrice() == 0)) {
            throw new ServiceException("积分配置不合法", 400);
        }
        memberConfigGateway.configSave(v);
        return true;
    }
    /** 订单创建、支付、关闭和售后资产写入同一会员事务，重复回调按业务编号核对。 */
    @Transactional(rollbackFor = Exception.class)
    public void tradeAsset(com.lemnt.member.application.growth.cmd.MemberTradeAssetCmd cmd) {
        if (cmd == null || cmd.usePoint() < 0 || cmd.givePoint() < 0 || cmd.paidFen() < 0
                || cmd.refundFen() < 0 || cmd.refundFen() > cmd.paidFen() || cmd.refundPoint() < 0) {
            throw new ServiceException("交易权益参数不合法", 400);
        }
        String orderNo = MemberRules.text(cmd.orderNo(), 40);
        var member = lock(cmd.memberId());
        String reason = "订单权益:" + cmd.usePoint() + ":" + cmd.givePoint() + ":" + cmd.paidFen();
        switch (cmd.operation()) {
            case "CREATE" -> {
                if (assetRequest(cmd.memberId(), "POINT", "TRADE_CLOSED", orderNo) != null) {
                    throw new ServiceException("订单已经关闭，不能再抵扣积分", 409);
                }
                if (cmd.usePoint() > 0) {
                    adjust(cmd.memberId(), "POINT", -cmd.usePoint(), "TRADE_USE", orderNo, reason, false, false);
                }
            }
            case "PAID" -> {
                if (cmd.usePoint() > 0 && assetRequest(
                        cmd.memberId(), "POINT", "TRADE_USE", orderNo) == null) {
                    throw new ServiceException("订单抵扣积分尚未确认", 409);
                }
                if (cmd.givePoint() > 0) {
                    adjust(cmd.memberId(), "POINT", cmd.givePoint(), "TRADE_GIVE", orderNo, reason, false, false);
                }
                if (cmd.paidFen() > 0) {
                    adjust(cmd.memberId(), "EXPERIENCE", cmd.paidFen(), "TRADE_EXPERIENCE", orderNo, reason,
                            false, true);
                }
            }
            case "CLOSED" -> {
                // 零变更终态记录阻止迟到的创建调用，不增加技术表。
                adjust(cmd.memberId(), "POINT", 0, "TRADE_CLOSED", orderNo, reason, true, false);
                var used = assetRequest(cmd.memberId(), "POINT", "TRADE_USE", orderNo);
                if (used != null) {
                    adjust(cmd.memberId(), "POINT", -used.delta(), "TRADE_USE_CANCEL", orderNo, reason, false, false);
                }
            }
            case "REFUND" -> {
                String refundNo = MemberRules.text(cmd.refundNo(), 40);
                String key = MemberRules.text(orderNo + ":" + refundNo, 64);
                String refundReason = reason + ":" + cmd.refundFen() + ":" + cmd.fullRefund();
                returnTradeAsset(cmd.memberId(), "POINT", "TRADE_USE", "TRADE_USE_RETURN", orderNo,
                        key, cmd.refundFen(), cmd.paidFen(), cmd.fullRefund(), false, cmd.refundPoint(),
                        refundReason);
                returnTradeAsset(cmd.memberId(), "POINT", "TRADE_GIVE", "TRADE_GIVE_RETURN", orderNo,
                        key, cmd.refundFen(), cmd.paidFen(), cmd.fullRefund(), true, 0L, refundReason);
                returnTradeAsset(cmd.memberId(), "EXPERIENCE", "TRADE_EXPERIENCE", "TRADE_EXPERIENCE_RETURN",
                        orderNo, key, cmd.refundFen(), cmd.paidFen(), cmd.fullRefund(), true, 0L, refundReason);
            }
            default -> throw new ServiceException("交易权益操作不合法", 400);
        }
    }

    /** 按已记录的原始权益回退；最终整单退款处理分次取整尾差，不回退其他订单权益。 */
    private void returnTradeAsset(Long memberId, String type, String originalType, String returnType,
            String orderNo, String key, long refundedFen, long paidFen, boolean fullRefund,
            boolean reclaim, long explicitPoints, String reason) {
        var previous = assetRequest(memberId, type, returnType, key);
        if (previous != null) {
            if (!reason.equals(previous.reason())) { throw new ServiceException("售后权益内容不一致", 409); }
            return;
        }
        var original = assetRequest(memberId, type, originalType, orderNo);
        if (original == null) { return; }
        long total = Math.abs(original.delta());
        long returned = Math.abs(tradeReturns(memberId, type, returnType, orderNo));
        if (returned > total) { throw new ServiceException("历史权益回退超过原记录", 409); }
        long planned;
        if (paidFen == 0) {
            // 零元订单由交易按数量比例计算本次退回积分，整单退款返回剩余全部积分。
            planned = fullRefund ? total - returned
                    : Math.min(Math.max(0L, explicitPoints), total - returned);
        } else {
            planned = java.math.BigInteger.valueOf(total)
                    .multiply(java.math.BigInteger.valueOf(refundedFen))
                    .divide(java.math.BigInteger.valueOf(paidFen)).longValueExact();
        }
        long delta = Math.min(total - returned, planned);
        if (reclaim) {
            var user = lock(memberId);
            // 已使用的赠送权益不产生负余额，也不阻塞原支付退款。
            delta = -Math.min(delta, "POINT".equals(type) ? user.points() : user.experience());
        }
        adjust(memberId, type, delta, returnType, key, reason, true, "EXPERIENCE".equals(type));
    }
}
