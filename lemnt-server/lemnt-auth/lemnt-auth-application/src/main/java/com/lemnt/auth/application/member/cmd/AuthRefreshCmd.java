package com.lemnt.auth.application.member.cmd;

import java.io.Serializable;

/**
 * 认证续期命令。
 */
public record AuthRefreshCmd(String accessToken) implements Serializable {
    @Override
    public String toString() {
        return "AuthRefreshCmd[redacted]";
    }
}
