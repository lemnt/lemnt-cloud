package com.lemnt.member.domain.user.model;

import java.time.LocalDateTime;
import java.util.List;

/** 会员列表专用筛选，标签采用任一命中语义。 */
public record MemberPageFilter(
        String mobile,
        String email,
        String nickname,
        Long levelId,
        Long groupId,
        List<Long> tagIds,
        LocalDateTime loginStartTime,
        LocalDateTime loginEndTime,
        LocalDateTime createStartTime,
        LocalDateTime createEndTime,
        int offset,
        int limit
) {}
