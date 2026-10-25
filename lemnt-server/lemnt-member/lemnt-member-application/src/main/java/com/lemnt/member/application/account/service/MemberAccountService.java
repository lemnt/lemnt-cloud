package com.lemnt.member.application.account.service;

import com.lemnt.member.domain.account.gateway.MemberAccountGateway;
import com.lemnt.member.domain.account.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Component
@RequiredArgsConstructor
public class MemberAccountService {
    private final MemberAccountGateway accounts;

    private Profile lock(Long id) {
        MemberRules.id(id);
        var value = accounts.lock(id);
        if (value == null) throw new IllegalArgumentException("会员不存在");
        return value;
    }

    private Profile active(Long id) {
        var value = lock(id);
        if (!Integer.valueOf(1).equals(value.status())) throw new SecurityException("会员已停用");
        return value;
    }

    public Profile profile(Long id) {
        MemberRules.id(id);
        return accounts.profile(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean profileUpdate(
            Long id,
            String nickname,
            String email,
            String avatar,
            Integer sex,
            java.time.LocalDate birthday,
            Integer status) {
        var old = lock(id);
        String name = MemberRules.text(nickname, 80);
        String mail = MemberRules.optional(email, 254);
        String image = MemberRules.optional(avatar, 500);
        if (mail != null && !mail.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))
            throw new IllegalArgumentException("邮箱不合法");
        if (image != null && !image.startsWith("https://"))
            throw new IllegalArgumentException("头像必须是HTTPS地址");
        if (sex != null && (sex < 0 || sex > 2)) throw new IllegalArgumentException("性别不合法");
        if (birthday != null && birthday.isAfter(accounts.now().toLocalDate()))
            throw new IllegalArgumentException("出生日期不能在未来");
        int state = status == null ? old.status() : status;
        if (state != 0 && state != 1) throw new IllegalArgumentException("状态不合法");
        accounts.profileUpdate(
                new Profile(
                        id,
                        old.memberNo(),
                        name,
                        old.mobile(),
                        mail,
                        image,
                        state,
                        old.points(),
                        old.experience(),
                        old.levelId(),
                        old.groupId(),
                        sex,
                        birthday,
                        old.createTime()));
        if (state == 0) accounts.sessionsRevoke(id);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Address saveAddress(Address input) {
        active(input.memberId());
        String name = MemberRules.text(input.name(), 80);
        String mobile = MemberRules.mobile(input.mobile());
        MemberRules.id(input.areaId());
        String detail = MemberRules.text(input.detailAddress(), 500);
        if (input.defaultStatus() == null) throw new IllegalArgumentException("默认地址标志不能为空");
        if (input.id() != null
                && accounts.address(input.memberId(), MemberRules.id(input.id())) == null)
            throw new IllegalArgumentException("地址不存在");
        if (input.id() == null && accounts.addresses(input.memberId()).size() >= 20)
            throw new IllegalStateException("最多20个地址");
        if (input.defaultStatus()) accounts.clearDefaultAddress(input.memberId());
        Long id =
                accounts.saveAddress(
                        new Address(
                                input.id(),
                                input.memberId(),
                                name,
                                mobile,
                                input.areaId(),
                                detail,
                                input.defaultStatus()));
        return accounts.address(input.memberId(), id);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long memberId, Long id) {
        active(memberId);
        return accounts.deleteAddress(memberId, MemberRules.id(id));
    }

    public List<Address> addresses(Long memberId) {
        MemberRules.id(memberId);
        return accounts.addresses(memberId);
    }

    public Address address(Long memberId, Long id) {
        MemberRules.id(memberId);
        return accounts.address(memberId, MemberRules.id(id));
    }

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
        if (!Set.of("POINT", "EXPERIENCE").contains(type))
            throw new IllegalArgumentException("资产类型不合法");
        bizType = MemberRules.text(bizType, 40);
        key = MemberRules.text(key, 64);
        reason = MemberRules.text(reason, 500);
        var previous = accounts.assetRequest(memberId, type, bizType, key);
        if (previous != null) {
            if (previous.delta() != delta || !previous.reason().equals(reason))
                throw new IllegalStateException("业务编号已用于不同内容");
            return previous;
        }
        if (delta == 0 && !allowZero) throw new IllegalArgumentException("变更量不能为0");
        long balance =
                Math.addExact("POINT".equals(type) ? user.points() : user.experience(), delta);
        if (balance < 0) throw new IllegalArgumentException("余额不足");
        accounts.balance(memberId, type, balance);
        var record =
                new AssetRecord(
                        null, memberId, type, delta, balance, bizType, key, reason, accounts.now());
        accounts.assetSave(record);
        if (autoLevel && "EXPERIENCE".equals(type)) {
            var best =
                    accounts.levels().stream()
                            .filter(v -> v.status() == 1 && v.experience() <= balance)
                            .max(Comparator.comparing(Level::level))
                            .orElse(null);
            if (best != null && !Objects.equals(best.id(), user.levelId())) {
                accounts.assignLevel(memberId, best.id());
                accounts.levelRecord(
                        new LevelRecord(
                                null, memberId, best.id(), balance, reason, accounts.now()));
            }
        }
        return accounts.assetRequest(memberId, type, bizType, key);
    }

    @Transactional(rollbackFor = Exception.class)
    public AssetRecord adjustAsset(
            Long memberId, String type, long delta, String requestNo, String reason) {
        return adjust(memberId, type, delta, "ADMIN", requestNo, reason, false, true);
    }

    public List<AssetRecord> assets(Long memberId, String type, int page, int size) {
        MemberRules.id(memberId);
        MemberRules.page(page, size);
        if (!Set.of("POINT", "EXPERIENCE").contains(type))
            throw new IllegalArgumentException("资产类型不合法");
        return accounts.assets(memberId, type, (page - 1) * size, size);
    }

    public long assetCount(Long id, String type) {
        return accounts.assetCount(id, type);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean assignLevel(Long memberId, Long levelId, String requestNo, String reason) {
        var user = lock(memberId);
        String key = MemberRules.text(requestNo, 64);
        String audit =
                MemberRules.text("[level=" + levelId + "] " + MemberRules.text(reason, 450), 500);
        var previous = accounts.assetRequest(memberId, "EXPERIENCE", "ADMIN_LEVEL", key);
        if (previous != null) {
            if (!audit.equals(previous.reason())) throw new IllegalStateException("业务编号已用于不同等级操作");
            return true;
        }
        Level level = levelId == null ? null : accounts.level(MemberRules.id(levelId));
        if (levelId != null && (level == null || level.status() != 1))
            throw new IllegalArgumentException("等级不存在或已停用");
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
        accounts.assignLevel(memberId, levelId);
        accounts.levelRecord(
                new LevelRecord(null, memberId, levelId, target, audit, accounts.now()));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean assignGroup(Long memberId, Long groupId) {
        lock(memberId);
        if (groupId != null) {
            var group = accounts.group(MemberRules.id(groupId));
            if (group == null || group.status() != 1)
                throw new IllegalArgumentException("分组不存在或已停用");
        }
        accounts.assignGroup(memberId, groupId);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean assignTags(Long memberId, List<Long> tags) {
        lock(memberId);
        if (tags == null || tags.size() > 100 || tags.stream().distinct().count() != tags.size())
            throw new IllegalArgumentException("标签不合法或重复");
        for (Long id : tags)
            if (accounts.tag(MemberRules.id(id)) == null)
                throw new IllegalArgumentException("标签不存在");
        accounts.memberTags(memberId, tags);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveLevel(Level v) {
        accounts.lockCatalog();
        if (v.id() != null && accounts.level(MemberRules.id(v.id())) == null)
            throw new IllegalArgumentException("等级不存在");
        String name = MemberRules.text(v.name(), 80);
        if (v.level() == null
                || v.level() < 1
                || v.experience() == null
                || v.experience() < 1
                || v.discountPercent() == null
                || v.discountPercent() < 0
                || v.discountPercent() > 100
                || v.status() == null
                || (v.status() != 0 && v.status() != 1))
            throw new IllegalArgumentException("等级参数不合法");
        for (var old : accounts.levels())
            if (!Objects.equals(old.id(), v.id())) {
                if (old.name().equals(name)
                        || old.level().equals(v.level())
                        || (old.level() < v.level() && old.experience() >= v.experience())
                        || (old.level() > v.level() && old.experience() <= v.experience()))
                    throw new IllegalArgumentException("等级名称、序号或经验门槛冲突");
            }
        return accounts.levelSave(
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

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteLevel(Long id) {
        return accounts.levelDelete(MemberRules.id(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveGroup(Group v) {
        if (v.id() != null && accounts.group(MemberRules.id(v.id())) == null)
            throw new IllegalArgumentException("分组不存在");
        if (v.status() == null || (v.status() != 0 && v.status() != 1))
            throw new IllegalArgumentException("状态不合法");
        return accounts.groupSave(
                new Group(
                        v.id(),
                        MemberRules.text(v.name(), 80),
                        MemberRules.optional(v.remark(), 500),
                        v.status()));
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGroup(Long id) {
        return accounts.groupDelete(MemberRules.id(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveTag(Tag v) {
        if (v.id() != null && accounts.tag(MemberRules.id(v.id())) == null)
            throw new IllegalArgumentException("标签不存在");
        return accounts.tagSave(
                new Tag(
                        v.id(),
                        MemberRules.text(v.name(), 80),
                        MemberRules.optional(v.remark(), 500)));
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTag(Long id) {
        return accounts.tagDelete(MemberRules.id(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean saveSignConfig(SignConfig v) {
        if (v.day() == null
                || v.day() < 1
                || v.day() > 366
                || v.point() == null
                || v.point() < 0
                || v.experience() == null
                || v.experience() < 0
                || (v.point() == 0 && v.experience() == 0)
                || v.status() == null
                || (v.status() != 0 && v.status() != 1))
            throw new IllegalArgumentException("签到配置不合法");
        accounts.signConfigSave(v);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSignConfig(Integer day) {
        if (day == null || day < 1) throw new IllegalArgumentException("天数不合法");
        return accounts.signConfigDelete(day);
    }

    @Transactional(rollbackFor = Exception.class)
    public SignRecord sign(Long memberId) {
        active(memberId);
        var today = accounts.now().toLocalDate();
        var previous = accounts.signDate(memberId, today);
        if (previous != null) return previous;
        var configs = accounts.signConfigs().stream().filter(v -> v.status() == 1).toList();
        if (configs.isEmpty()) throw new IllegalStateException("签到规则未启用");
        int max = configs.stream().mapToInt(SignConfig::day).max().orElse(1);
        var last = accounts.lastSign(memberId);
        int day = last != null && last.signDate().equals(today.minusDays(1)) ? last.day() + 1 : 1;
        if (day > max) day = 1;
        final int current = day;
        var config =
                configs.stream()
                        .filter(v -> v.day() == current)
                        .findFirst()
                        .orElse(new SignConfig(day, 0L, 0L, 1));
        accounts.signSave(
                new SignRecord(null, memberId, today, day, config.point(), config.experience()));
        if (config.point() > 0)
            adjust(
                    memberId,
                    "POINT",
                    config.point(),
                    "SIGN_IN",
                    today.toString(),
                    "每日签到",
                    false,
                    true);
        if (config.experience() > 0)
            adjust(
                    memberId,
                    "EXPERIENCE",
                    config.experience(),
                    "SIGN_IN",
                    today.toString(),
                    "每日签到",
                    false,
                    true);
        return accounts.signDate(memberId, today);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean saveConfig(Config v) {
        if (v.pointTradeDeductEnable() == null
                || v.pointTradeDeductUnitPrice() == null
                || v.pointTradeDeductUnitPrice() < 0
                || v.pointTradeDeductMaxPrice() == null
                || v.pointTradeDeductMaxPrice() < 0
                || v.pointTradeGivePoint() == null
                || v.pointTradeGivePoint() < 0
                || (v.pointTradeDeductEnable() && v.pointTradeDeductUnitPrice() == 0))
            throw new IllegalArgumentException("积分配置不合法");
        accounts.configSave(v);
        return true;
    }
}
