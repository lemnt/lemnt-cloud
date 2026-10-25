package com.lemnt.member.application.account.executor;

import com.lemnt.member.application.account.qry.*;
import com.lemnt.member.application.account.dto.*;
import com.lemnt.member.application.account.converter.MemberDTOConverter;
import com.lemnt.member.domain.account.gateway.MemberAccountGateway;
import com.lemnt.member.domain.account.model.*;
import java.util.List;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class MemberAccountQueryExe {
    private final MemberAccountGateway accounts;

    public ProfileDTO execute(ProfileQry q) {
        MemberRules.id(q.memberId());
        return MemberDTOConverter.profile(accounts.profile(q.memberId()));
    }

    public AccountPageDTO<ProfileDTO> execute(ProfilesPageQry q) {
        MemberRules.page(q.pageNum(), q.pageSize());
        String mobile = MemberRules.optional(q.mobile(), 24);
        return new AccountPageDTO<>(
                accounts.profiles(mobile, (q.pageNum() - 1) * q.pageSize(), q.pageSize()).stream()
                        .map(MemberDTOConverter::profile)
                        .toList(),
                accounts.profileCount(mobile));
    }

    public List<AddressDTO> execute(AddressListQry q) {
        MemberRules.id(q.memberId());
        return accounts.addresses(q.memberId()).stream().map(MemberDTOConverter::address).toList();
    }

    public AddressDTO execute(AddressDetailQry q) {
        MemberRules.id(q.memberId());
        MemberRules.id(q.id());
        return MemberDTOConverter.address(accounts.address(q.memberId(), q.id()));
    }

    public AccountPageDTO<AssetRecordDTO> execute(AssetsPageQry q) {
        MemberRules.id(q.memberId());
        MemberRules.page(q.pageNum(), q.pageSize());
        if (!java.util.Set.of("POINT", "EXPERIENCE").contains(q.assetType()))
            throw new IllegalArgumentException("资产类型不合法");
        return new AccountPageDTO<>(
                accounts
                        .assets(
                                q.memberId(),
                                q.assetType(),
                                (q.pageNum() - 1) * q.pageSize(),
                                q.pageSize())
                        .stream()
                        .map(MemberDTOConverter::assetRecord)
                        .toList(),
                accounts.assetCount(q.memberId(), q.assetType()));
    }

    public List<Long> execute(MemberTagsQry q) {
        MemberRules.id(q.memberId());
        return accounts.memberTags(q.memberId());
    }

    public List<LevelDTO> execute(LevelsQry q) {
        return accounts.levels().stream()
                .filter(v -> !q.enabledOnly() || v.status() == 1)
                .map(MemberDTOConverter::level)
                .toList();
    }

    public LevelDTO execute(LevelDetailQry q) {
        MemberRules.id(q.id());
        return MemberDTOConverter.level(accounts.level(q.id()));
    }

    public List<GroupDTO> execute(GroupsQry q) {
        return accounts.groups().stream().map(MemberDTOConverter::group).toList();
    }

    public GroupDTO execute(GroupDetailQry q) {
        MemberRules.id(q.id());
        return MemberDTOConverter.group(accounts.group(q.id()));
    }

    public List<TagDTO> execute(TagsQry q) {
        return accounts.tags().stream().map(MemberDTOConverter::tag).toList();
    }

    public TagDTO execute(TagDetailQry q) {
        MemberRules.id(q.id());
        return MemberDTOConverter.tag(accounts.tag(q.id()));
    }

    public List<SignConfigDTO> execute(SignConfigsQry q) {
        return accounts.signConfigs().stream()
                .filter(v -> !q.enabledOnly() || v.status() == 1)
                .map(MemberDTOConverter::signConfig)
                .toList();
    }

    public SignSummaryDTO execute(SignSummaryQry q) {
        MemberRules.id(q.memberId());
        var today = accounts.now().toLocalDate();
        var last = accounts.lastSign(q.memberId());
        int continuous =
                last != null && !last.signDate().isBefore(today.minusDays(1)) ? last.day() : 0;
        return new SignSummaryDTO(
                accounts.signCount(q.memberId()),
                continuous,
                last != null && last.signDate().equals(today));
    }

    public AccountPageDTO<SignRecordDTO> execute(SignsPageQry q) {
        MemberRules.id(q.memberId());
        MemberRules.page(q.pageNum(), q.pageSize());
        return new AccountPageDTO<>(
                accounts
                        .signs(q.memberId(), (q.pageNum() - 1) * q.pageSize(), q.pageSize())
                        .stream()
                        .map(MemberDTOConverter::signRecord)
                        .toList(),
                accounts.signCount(q.memberId()));
    }

    public AccountPageDTO<LevelRecordDTO> execute(LevelRecordsPageQry q) {
        MemberRules.id(q.memberId());
        MemberRules.page(q.pageNum(), q.pageSize());
        return new AccountPageDTO<>(
                accounts
                        .levelRecords(q.memberId(), (q.pageNum() - 1) * q.pageSize(), q.pageSize())
                        .stream()
                        .map(MemberDTOConverter::levelRecord)
                        .toList(),
                accounts.levelRecordCount(q.memberId()));
    }

    public ConfigDTO execute(ConfigQry q) {
        return MemberDTOConverter.config(accounts.config());
    }

    public AddressDTO execute(DefaultAddressQry q) {
        MemberRules.id(q.memberId());
        return accounts.addresses(q.memberId()).stream()
                .filter(v -> Boolean.TRUE.equals(v.defaultStatus()))
                .findFirst()
                .map(MemberDTOConverter::address)
                .orElse(null);
    }

    public SignConfigDTO execute(SignConfigDetailQry q) {
        if (q.day() == null || q.day() < 1 || q.day() > 366)
            throw new IllegalArgumentException("签到天数不合法");
        return accounts.signConfigs().stream()
                .filter(v -> v.day().equals(q.day()))
                .findFirst()
                .map(MemberDTOConverter::signConfig)
                .orElse(null);
    }
}
