package com.lemnt.member.application.user.dto;

import com.lemnt.member.domain.user.model.MemberStatistics.Distribution;
import com.lemnt.member.domain.user.model.MemberStatistics.Registration;

import java.util.List;

/** 新增、性别和地区统计。分布使用当前档案的全量人数。 */
public record MemberStatisticsDTO(
        List<Registration> registrations, List<Distribution> sexes, List<Distribution> areas) {}
