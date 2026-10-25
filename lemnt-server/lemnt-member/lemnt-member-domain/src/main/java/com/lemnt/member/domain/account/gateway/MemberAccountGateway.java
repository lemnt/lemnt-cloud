package com.lemnt.member.domain.account.gateway;

import com.lemnt.member.domain.account.model.*;
import java.util.List;
import java.time.LocalDateTime;

public interface MemberAccountGateway {
    LocalDateTime now();

    void lockCatalog();

    LoginAttempt loginAttempt(String hash);

    void loginAttemptUpdate(LoginAttempt value);

    Profile profile(Long id);

    Profile lock(Long id);

    List<Profile> profiles(String mobile, int offset, int limit);

    long profileCount(String mobile);

    void profileUpdate(Profile value);

    List<Address> addresses(Long memberId);

    Address address(Long memberId, Long id);

    Long saveAddress(Address value);

    boolean deleteAddress(Long memberId, Long id);

    void clearDefaultAddress(Long memberId);

    Credential credential(String mobile);

    Credential credentialForMember(Long memberId);

    void password(Long memberId, String hash);

    void sessionSave(Session value);

    Session sessionAccess(String hash);

    Session sessionRefresh(String hash);

    Session lockSession(String hash);

    void sessionDelete(String id);

    void sessionsRevoke(Long memberId);

    AssetRecord assetRequest(Long memberId, String assetType, String bizType, String bizNo);

    void assetSave(AssetRecord record);

    void balance(Long memberId, String assetType, Long balance);

    List<AssetRecord> assets(Long memberId, String type, int offset, int limit);

    long assetCount(Long memberId, String type);

    List<Level> levels();

    Level level(Long id);

    Long levelSave(Level value);

    boolean levelDelete(Long id);

    void assignLevel(Long memberId, Long levelId);

    void levelRecord(LevelRecord value);

    List<LevelRecord> levelRecords(Long memberId, int offset, int limit);

    long levelRecordCount(Long memberId);

    List<Group> groups();

    Group group(Long id);

    Long groupSave(Group value);

    boolean groupDelete(Long id);

    void assignGroup(Long memberId, Long groupId);

    List<Tag> tags();

    Tag tag(Long id);

    Long tagSave(Tag value);

    boolean tagDelete(Long id);

    List<Long> memberTags(Long memberId);

    void memberTags(Long memberId, List<Long> tags);

    List<SignConfig> signConfigs();

    void signConfigSave(SignConfig value);

    boolean signConfigDelete(Integer day);

    SignRecord signDate(Long memberId, java.time.LocalDate date);

    SignRecord lastSign(Long memberId);

    void signSave(SignRecord value);

    List<SignRecord> signs(Long memberId, int offset, int limit);

    long signCount(Long memberId);

    Config config();

    void configSave(Config value);
}
