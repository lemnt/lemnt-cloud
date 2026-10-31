package com.lemnt.member.application.growth.cmd;

import java.io.Serializable;

/**
 * 签到创建命令。
 */
public record SignCreateCmd(
        Long memberId
) implements Serializable {}
