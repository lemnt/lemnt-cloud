package com.lemnt.member.application.account.dto;

public record TokenPairDTO(Long memberId, String accessToken, String refreshToken, Long expiresIn) {
    @Override
    public String toString() {
        return "TokenPairDTO[tokens=<redacted>]";
    }
}
