package com.lemnt.auth.application.auth.cmd;

import java.io.Serializable;

/**
 * 用户登录命令。
 *
 * @author wangxx
 */
public record LoginCmd(String username, String password) implements Serializable {
}
