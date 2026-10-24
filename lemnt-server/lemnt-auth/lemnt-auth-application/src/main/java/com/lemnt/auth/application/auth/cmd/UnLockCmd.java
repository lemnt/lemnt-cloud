package com.lemnt.auth.application.auth.cmd;

import java.io.Serializable;

/**
 * 系统解锁命令。
 *
 * @author wangxx
 */
public record UnLockCmd(String password) implements Serializable {
}
