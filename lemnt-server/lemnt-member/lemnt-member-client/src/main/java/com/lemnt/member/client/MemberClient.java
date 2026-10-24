package com.lemnt.member.client;
import com.lemnt.member.client.dto.MemberProfileDTO;
public interface MemberClient { MemberProfileDTO findById(Long memberId); }