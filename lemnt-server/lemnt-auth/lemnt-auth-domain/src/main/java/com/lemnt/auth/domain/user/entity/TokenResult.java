package com.lemnt.auth.domain.user.entity;

import java.io.Serializable;

/**
 * Token 创建结果（值对象）。
 *
 * @author wangxx
 */
public record TokenResult(String accessToken, Long expiresIn) implements Serializable {
}
