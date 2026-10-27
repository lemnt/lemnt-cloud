package com.lemnt.member.application.user.cmd;

import java.io.Serializable;

/**
 * 密码修改命令。
 */
public record PasswordChangeCmd(
        Long memberId,
        String oldPassword,
        String newPassword
) implements Serializable {
    @Override
    public String toString() {
        return "PasswordChangeCmd[redacted]";
    }
}
