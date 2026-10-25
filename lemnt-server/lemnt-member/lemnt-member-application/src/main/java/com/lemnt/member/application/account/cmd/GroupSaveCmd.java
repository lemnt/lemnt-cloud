package com.lemnt.member.application.account.cmd;

public record GroupSaveCmd(Long id, String name, String remark, Integer status) {}
