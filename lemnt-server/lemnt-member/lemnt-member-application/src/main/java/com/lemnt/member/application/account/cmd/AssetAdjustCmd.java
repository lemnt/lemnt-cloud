package com.lemnt.member.application.account.cmd;

public record AssetAdjustCmd(
        Long memberId, String assetType, Long delta, String requestNo, String reason) {}
