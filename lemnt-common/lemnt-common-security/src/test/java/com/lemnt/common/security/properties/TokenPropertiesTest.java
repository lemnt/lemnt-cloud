package com.lemnt.common.security.properties;

import com.lemnt.common.security.config.TokenConfig;
import com.lemnt.common.security.utils.JwtUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 令牌配置测试
 *
 * <p>密钥缺失或强度不足时必须让应用启动失败，而不是回退到内置密钥静默运行。</p>
 *
 * @author wangxx
 */
class TokenPropertiesTest {

    private static final String VALID_SECRET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-!";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TokenConfig.class);

    @Test
    @DisplayName("密钥未配置：应用启动失败")
    void missingSecretShouldFailStartup() {
        runner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalStateException.class)
                    .rootCause()
                    .hasMessageContaining("JWT 密钥未配置");
        });
    }

    @Test
    @DisplayName("密钥为空串：应用启动失败")
    void blankSecretShouldFailStartup() {
        runner.withPropertyValues("security.token.secret=")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(IllegalStateException.class);
                });
    }

    @Test
    @DisplayName("密钥不足 64 字节：应用启动失败")
    void shortSecretShouldFailStartup() {
        runner.withPropertyValues("security.token.secret=" + "a".repeat(TokenProperties.MIN_SECRET_BYTES - 1))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(IllegalStateException.class)
                            .rootCause()
                            .hasMessageContaining("JWT 密钥长度不足");
                });
    }

    @Test
    @DisplayName("密钥恰好 64 字节：启动成功")
    void minLengthSecretShouldStart() {
        runner.withPropertyValues("security.token.secret=" + "a".repeat(TokenProperties.MIN_SECRET_BYTES))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(JwtUtils.class);
                });
    }

    @Test
    @DisplayName("密钥合法：注册 JwtUtils 单例")
    void validSecretShouldRegisterJwtUtils() {
        runner.withPropertyValues("security.token.secret=" + VALID_SECRET)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(JwtUtils.class);
                });
    }
}