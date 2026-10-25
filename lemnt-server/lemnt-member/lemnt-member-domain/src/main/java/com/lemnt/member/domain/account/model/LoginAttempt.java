package com.lemnt.member.domain.account.model;

public record LoginAttempt(
        String loginHash, Integer failures, java.time.LocalDateTime windowStart) {}
