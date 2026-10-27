package com.lemnt.auth.application.member.cmd;

/**
 * 认证退出命令。
 */
public record AuthLogoutCmd(
        String authorization
) implements java.io.Serializable {
    @Override
    public String toString() {
        return "AuthLogoutCmd[redacted]";
    }
}
