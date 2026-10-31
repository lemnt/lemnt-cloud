package com.lemnt.member.application.address.executor;

import com.lemnt.member.application.address.cmd.AddressDeleteCmd;
import com.lemnt.member.application.address.service.MemberAddressService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 地址删除命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberAddressDeleteCmdExe {

    private final MemberAddressService memberAddressService;

    /**
     * 执行地址删除命令。
     */
    public Boolean execute(AddressDeleteCmd cmd) {
        return memberAddressService.deleteAddress(cmd.memberId(), cmd.id());
    }
}
