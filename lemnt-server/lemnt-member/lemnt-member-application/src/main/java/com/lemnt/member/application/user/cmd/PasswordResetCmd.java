package com.lemnt.member.application.user.cmd;

import java.io.Serializable;

/**
 * 密码重置命令。
 */
public record PasswordResetCmd(
        Long memberId,
        String password
) implements Serializable {
    @Override
    public String toString() {
        return "PasswordResetCmd[redacted]";
    }
}
