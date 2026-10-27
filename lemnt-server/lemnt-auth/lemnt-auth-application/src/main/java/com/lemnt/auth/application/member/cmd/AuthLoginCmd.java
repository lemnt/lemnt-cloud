package com.lemnt.auth.application.member.cmd;

/**
 * 认证登录命令。
 */
public record AuthLoginCmd(
        String mobile,
        String password
) implements java.io.Serializable {
    @Override
    public String toString() {
        return "AuthLoginCmd[redacted]";
    }
}
