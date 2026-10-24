package com.lemnt.trade.domain.gateway;
import com.lemnt.trade.domain.model.MemberSnapshot;
public interface MemberLookupGateway { MemberSnapshot findById(Long memberId); }