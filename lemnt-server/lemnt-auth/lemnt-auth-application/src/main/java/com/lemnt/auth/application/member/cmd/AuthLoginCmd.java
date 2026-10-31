package com.lemnt.auth.application.member.cmd;

/**
 * 认证登录命令。
 */
public record AuthLoginCmd(
        String mobile,
        String password,
        String loginIp
) implements java.io.Serializable {
    /** 旧调用缺少连接信息时保持未知。 */
    public AuthLoginCmd(String mobile,String password) {this(mobile,password,null);}
    @Override
    public String toString() {
        return "AuthLoginCmd[redacted]";
    }
}
