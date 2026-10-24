package com.lemnt.member.client.dto;
import java.io.Serializable;
public record MemberProfileDTO(Long id, String memberNo, String nickname, Integer status) implements Serializable { }