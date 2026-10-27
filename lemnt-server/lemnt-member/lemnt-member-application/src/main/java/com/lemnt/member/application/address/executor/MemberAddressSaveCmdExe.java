package com.lemnt.member.application.address.executor;

import com.lemnt.member.application.address.cmd.AddressSaveCmd;
import com.lemnt.member.application.address.converter.MemberAddressDTOConverter;
import com.lemnt.member.application.address.dto.AddressDTO;
import com.lemnt.member.application.address.service.MemberAddressService;
import com.lemnt.member.domain.address.model.Address;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 地址保存命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberAddressSaveCmdExe {

    private final MemberAddressService memberAddressService;

    /**
     * 执行地址保存命令。
     */
    public AddressDTO execute(AddressSaveCmd cmd) {
        return MemberAddressDTOConverter.toAddressDTO(
                memberAddressService.saveAddress(
                        new Address(
                                cmd.id(),
                                cmd.memberId(),
                                cmd.name(),
                                cmd.mobile(),
                                cmd.areaId(),
                                cmd.detailAddress(),
                                cmd.defaultStatus())));
    }
}
