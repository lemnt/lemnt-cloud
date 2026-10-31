package com.lemnt.member.application.growth.cmd;

import java.io.Serializable;

/**
 * 资产调整命令。
 */
public record AssetAdjustCmd(
        Long memberId,
        String assetType,
        Long delta,
        String requestNo,
        String reason
) implements Serializable {}
