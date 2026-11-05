package com.lemnt.auth.domain.member.model;

/**
 * 会员展示资料快照，写入会话供网关向会员请求注入昵称/头像头。
 */
public record MemberProfile(String nickName, String avatar) {
}
