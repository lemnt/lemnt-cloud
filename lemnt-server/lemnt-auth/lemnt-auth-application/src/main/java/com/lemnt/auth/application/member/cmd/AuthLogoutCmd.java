package com.lemnt.auth.application.member.cmd;

import java.io.Serializable;

/**
 * 认证退出命令。
 */
public record AuthLogoutCmd(String authorization) implements Serializable {
    @Override
    public String toString() {
        return "AuthLogoutCmd[redacted]";
    }
}
