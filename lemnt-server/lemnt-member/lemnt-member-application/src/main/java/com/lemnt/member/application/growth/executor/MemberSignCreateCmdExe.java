package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.cmd.SignCreateCmd;
import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.SignRecordDTO;
import com.lemnt.member.application.growth.service.MemberGrowthService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 签到创建命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberSignCreateCmdExe {

    private final MemberGrowthService memberGrowthService;

    /**
     * 执行签到创建命令。
     */
    public SignRecordDTO execute(SignCreateCmd cmd) {
        return MemberGrowthDTOConverter.toSignRecordDTO(memberGrowthService.sign(cmd.memberId()));
    }
}
