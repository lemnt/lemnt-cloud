package com.lemnt.member.application.account.qry;

public record AssetsPageQry(Long memberId, String assetType, int pageNum, int pageSize) {}
