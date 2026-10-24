package com.lemnt.member.domain.gateway;
import com.lemnt.member.domain.model.Member;
public interface MemberGateway { Member findById(Long id); Member findByMobile(String mobile); Member findByEmail(String email); Member save(Member member); }