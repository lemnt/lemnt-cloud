package com.lemnt.gateway.service;

public interface MemberSessionLookup {
    Long authenticate(String token);
}
