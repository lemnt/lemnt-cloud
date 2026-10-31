package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.converter.MemberUserDTOConverter;
import com.lemnt.member.application.user.dto.TagDTO;
import com.lemnt.member.application.user.qry.TagListQry;
import com.lemnt.member.domain.user.gateway.MemberTagGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 查询标签查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberTagListQueryExe {

    private final MemberTagGateway memberTagGateway;

    /**
     * 执行查询标签查询。
     */
    public List<TagDTO> execute(TagListQry qry) {
        return memberTagGateway.tags().stream().map(MemberUserDTOConverter::toTagDTO).toList();
    }
}
