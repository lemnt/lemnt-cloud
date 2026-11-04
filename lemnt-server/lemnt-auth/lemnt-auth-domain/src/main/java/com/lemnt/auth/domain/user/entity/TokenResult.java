package com.lemnt.auth.domain.user.entity;

import java.io.Serializable;

/**
 * Token 创建结果（值对象）。
 * expiresIn 为本次签发的会话有效秒数，后续续期以 Redis 会话为准。
 *
 * @author wangxx
 */
public record TokenResult(String accessToken, Long expiresIn) implements Serializable {
}
