package com.lemnt.member.infrastructure.account.gateway;

import com.lemnt.member.domain.account.gateway.MemberAccountGateway;
import com.lemnt.member.domain.account.model.*;
import com.lemnt.member.infrastructure.account.converter.*;
import com.lemnt.member.infrastructure.account.mapper.MemberAccountMapper;
import java.util.List;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class MemberAccountGatewayImpl implements MemberAccountGateway {
    private final MemberAccountMapper mapper;

    public void lockCatalog() {
        if (mapper.lockCatalog() == null) throw new IllegalStateException("会员配置缺失");
    }

    public java.time.LocalDateTime now() {
        return mapper.now();
    }

    public Profile profile(Long id) {
        return ProfileConverter.domain(mapper.profile(id));
    }

    public Profile lock(Long id) {
        return ProfileConverter.domain(mapper.lock(id));
    }

    public List<Profile> profiles(String mobile, int offset, int limit) {
        return mapper.profiles(mobile, offset, limit).stream()
                .map(ProfileConverter::domain)
                .toList();
    }

    public long profileCount(String mobile) {
        return mapper.profileCount(mobile);
    }

    public void profileUpdate(Profile value) {
        mapper.profileUpdate(ProfileConverter.row(value));
    }

    public List<Address> addresses(Long memberId) {
        return mapper.addresses(memberId).stream().map(AddressConverter::domain).toList();
    }

    public Address address(Long memberId, Long id) {
        return AddressConverter.domain(mapper.address(memberId, id));
    }

    public Long saveAddress(Address value) {
        var row = AddressConverter.row(value);
        if (value.id() == null) mapper.addressInsert(row);
        else if (mapper.addressUpdate(row) != 1) throw new IllegalStateException("地址不存在");
        return row.getId();
    }

    public boolean deleteAddress(Long memberId, Long id) {
        return mapper.addressDelete(memberId, id) == 1;
    }

    public void clearDefaultAddress(Long memberId) {
        mapper.clearDefaultAddress(memberId);
    }

    public Credential credential(String mobile) {
        return CredentialConverter.domain(mapper.credential(mobile));
    }

    public Credential credentialForMember(Long memberId) {
        return CredentialConverter.domain(mapper.credentialForMember(memberId));
    }

    public void password(Long memberId, String hash) {
        mapper.password(memberId, hash);
    }

    public void sessionSave(Session value) {
        mapper.sessionSave(SessionConverter.row(value));
    }

    public Session sessionAccess(String hash) {
        return SessionConverter.domain(mapper.sessionAccess(hash));
    }

    public Session sessionRefresh(String hash) {
        return SessionConverter.domain(mapper.sessionRefresh(hash));
    }

    public Session lockSession(String hash) {
        return SessionConverter.domain(mapper.lockSession(hash));
    }

    public void sessionDelete(String id) {
        mapper.sessionDelete(id);
    }

    public void sessionsRevoke(Long memberId) {
        mapper.sessionsRevoke(memberId);
    }

    public AssetRecord assetRequest(Long memberId, String type, String bizType, String bizNo) {
        return AssetRecordConverter.domain(mapper.assetRequest(memberId, type, bizType, bizNo));
    }

    public void assetSave(AssetRecord value) {
        mapper.assetSave(AssetRecordConverter.row(value));
    }

    public void balance(Long memberId, String type, Long balance) {
        if ("POINT".equals(type)) mapper.points(memberId, balance);
        else if ("EXPERIENCE".equals(type)) mapper.experience(memberId, balance);
        else throw new IllegalArgumentException("资产类型不合法");
    }

    public List<AssetRecord> assets(Long memberId, String type, int offset, int limit) {
        return mapper.assets(memberId, type, offset, limit).stream()
                .map(AssetRecordConverter::domain)
                .toList();
    }

    public long assetCount(Long memberId, String type) {
        return mapper.assetCount(memberId, type);
    }

    public void assignLevel(Long memberId, Long levelId) {
        mapper.assignLevel(memberId, levelId);
    }

    public void levelRecord(LevelRecord value) {
        mapper.levelRecord(LevelRecordConverter.row(value));
    }

    public List<LevelRecord> levelRecords(Long memberId, int offset, int limit) {
        return mapper.levelRecords(memberId, offset, limit).stream()
                .map(LevelRecordConverter::domain)
                .toList();
    }

    public long levelRecordCount(Long memberId) {
        return mapper.levelRecordCount(memberId);
    }

    public void assignGroup(Long memberId, Long groupId) {
        mapper.assignGroup(memberId, groupId);
    }

    public List<Long> memberTags(Long memberId) {
        return mapper.memberTags(memberId);
    }

    public void memberTags(Long memberId, List<Long> tags) {
        mapper.clearTags(memberId);
        for (Long tag : tags) mapper.addTag(memberId, tag);
    }

    public List<SignConfig> signConfigs() {
        return mapper.signConfigs().stream().map(SignConfigConverter::domain).toList();
    }

    public void signConfigSave(SignConfig value) {
        mapper.signConfigSave(SignConfigConverter.row(value));
    }

    public boolean signConfigDelete(Integer day) {
        return mapper.signConfigDelete(day) == 1;
    }

    public SignRecord signDate(Long memberId, java.time.LocalDate date) {
        return SignRecordConverter.domain(mapper.signDate(memberId, date));
    }

    public SignRecord lastSign(Long memberId) {
        return SignRecordConverter.domain(mapper.lastSign(memberId));
    }

    public void signSave(SignRecord value) {
        mapper.signSave(SignRecordConverter.row(value));
    }

    public List<SignRecord> signs(Long memberId, int offset, int limit) {
        return mapper.signs(memberId, offset, limit).stream()
                .map(SignRecordConverter::domain)
                .toList();
    }

    public long signCount(Long memberId) {
        return mapper.signCount(memberId);
    }

    public Config config() {
        return ConfigConverter.domain(mapper.config());
    }

    public void configSave(Config value) {
        mapper.configSave(ConfigConverter.row(value));
    }

    public List<Level> levels() {
        return mapper.levels().stream().map(LevelConverter::domain).toList();
    }

    public Level level(Long id) {
        return LevelConverter.domain(mapper.level(id));
    }

    public Long levelSave(Level value) {
        var row = LevelConverter.row(value);
        if (value.id() == null) mapper.levelInsert(row);
        else if (mapper.levelUpdate(row) != 1) throw new IllegalStateException("记录不存在");
        return row.getId();
    }

    public boolean levelDelete(Long id) {
        return mapper.levelDelete(id) == 1;
    }

    public List<Group> groups() {
        return mapper.groups().stream().map(GroupConverter::domain).toList();
    }

    public Group group(Long id) {
        return GroupConverter.domain(mapper.group(id));
    }

    public Long groupSave(Group value) {
        var row = GroupConverter.row(value);
        if (value.id() == null) mapper.groupInsert(row);
        else if (mapper.groupUpdate(row) != 1) throw new IllegalStateException("记录不存在");
        return row.getId();
    }

    public boolean groupDelete(Long id) {
        return mapper.groupDelete(id) == 1;
    }

    public List<Tag> tags() {
        return mapper.tags().stream().map(TagConverter::domain).toList();
    }

    public Tag tag(Long id) {
        return TagConverter.domain(mapper.tag(id));
    }

    public Long tagSave(Tag value) {
        var row = TagConverter.row(value);
        if (value.id() == null) mapper.tagInsert(row);
        else if (mapper.tagUpdate(row) != 1) throw new IllegalStateException("记录不存在");
        return row.getId();
    }

    public boolean tagDelete(Long id) {
        return mapper.tagDelete(id) == 1;
    }

    public LoginAttempt loginAttempt(String hash) {
        mapper.ensureLogin(hash);
        var v = mapper.loginAttempt(hash);
        return new LoginAttempt(v.getLoginHash(), v.getFailures(), v.getWindowStart());
    }

    public void loginAttemptUpdate(LoginAttempt value) {
        var row = new com.lemnt.member.infrastructure.account.dataobject.LoginAttemptDO();
        row.setLoginHash(value.loginHash());
        row.setFailures(value.failures());
        row.setWindowStart(value.windowStart());
        mapper.loginAttemptUpdate(row);
    }
}
