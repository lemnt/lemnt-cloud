package com.lemnt.member.application.account.executor;

import com.lemnt.member.application.account.cmd.*;
import com.lemnt.member.application.account.dto.*;
import com.lemnt.member.application.account.converter.MemberDTOConverter;
import com.lemnt.member.application.account.service.*;
import com.lemnt.member.domain.account.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberAccountCmdExe {
    private final MemberAccountService service;

    public Boolean execute(ProfileUpdateCmd c) {
        return service.profileUpdate(
                c.memberId(),
                c.nickname(),
                c.email(),
                c.avatar(),
                c.sex(),
                c.birthday(),
                c.status());
    }

    public AddressDTO execute(AddressSaveCmd c) {
        return MemberDTOConverter.address(
                service.saveAddress(
                        new Address(
                                c.id(),
                                c.memberId(),
                                c.name(),
                                c.mobile(),
                                c.areaId(),
                                c.detailAddress(),
                                c.defaultStatus())));
    }

    public Boolean execute(AddressDeleteCmd c) {
        return service.deleteAddress(c.memberId(), c.id());
    }

    public AssetRecordDTO execute(AssetAdjustCmd c) {
        return MemberDTOConverter.assetRecord(
                service.adjustAsset(
                        c.memberId(), c.assetType(), c.delta(), c.requestNo(), c.reason()));
    }

    public Boolean execute(LevelAssignCmd c) {
        return service.assignLevel(c.memberId(), c.levelId(), c.requestNo(), c.reason());
    }

    public Boolean execute(GroupAssignCmd c) {
        return service.assignGroup(c.memberId(), c.groupId());
    }

    public Boolean execute(TagsAssignCmd c) {
        return service.assignTags(c.memberId(), c.tagIds());
    }

    public SignRecordDTO execute(SignCreateCmd c) {
        return MemberDTOConverter.signRecord(service.sign(c.memberId()));
    }

    public Long execute(LevelSaveCmd c) {
        return service.saveLevel(
                new Level(
                        c.id(),
                        c.name(),
                        c.level(),
                        c.experience(),
                        c.discountPercent(),
                        c.icon(),
                        c.backgroundUrl(),
                        c.status()));
    }

    public Long execute(GroupSaveCmd c) {
        return service.saveGroup(new Group(c.id(), c.name(), c.remark(), c.status()));
    }

    public Long execute(TagSaveCmd c) {
        return service.saveTag(new Tag(c.id(), c.name(), c.remark()));
    }

    public Boolean execute(SignConfigSaveCmd c) {
        return service.saveSignConfig(
                new SignConfig(c.day(), c.point(), c.experience(), c.status()));
    }

    public Boolean execute(ConfigSaveCmd c) {
        return service.saveConfig(
                new Config(
                        c.pointTradeDeductEnable(),
                        c.pointTradeDeductUnitPrice(),
                        c.pointTradeDeductMaxPrice(),
                        c.pointTradeGivePoint()));
    }

    public Boolean execute(LevelDeleteCmd c) {
        return service.deleteLevel(c.id());
    }

    public Boolean execute(GroupDeleteCmd c) {
        return service.deleteGroup(c.id());
    }

    public Boolean execute(TagDeleteCmd c) {
        return service.deleteTag(c.id());
    }

    public Boolean execute(SignConfigDeleteCmd c) {
        return service.deleteSignConfig(c.day());
    }
}
