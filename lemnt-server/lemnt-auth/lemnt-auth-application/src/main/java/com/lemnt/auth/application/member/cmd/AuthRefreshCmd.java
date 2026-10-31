package com.lemnt.auth.application.member.cmd;

/**
 * 认证续期命令。
 */
public record AuthRefreshCmd(
        String accessToken
) implements java.io.Serializable {
    @Override
    public String toString() {
        return "AuthRefreshCmd[redacted]";
    }
}
