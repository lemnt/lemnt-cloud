package com.lemnt.common.swagger.config;

import com.lemnt.common.swagger.config.properties.SpringDocProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpringDocAutoConfigurationTest {

    @Test
    void shouldCreateOpenApi31ContractWithBearerAndProblemDetails() {
        SpringDocProperties properties = new SpringDocProperties();
        properties.setGatewayUrl("http://localhost:8080");

        var openApi = new SpringDocAutoConfiguration().openApi(properties);

        assertThat(openApi.getOpenapi()).isEqualTo("3.1.0");
        assertThat(openApi.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
        assertThat(openApi.getComponents().getSecuritySchemes().get("bearerAuth").getScheme())
                .isEqualTo("bearer");
        assertThat(openApi.getComponents().getSchemas())
                .containsKeys("ProblemDetails", "ValidationProblemDetails");
        assertThat(openApi.getComponents().getResponses())
                .containsKeys("Unauthorized", "Forbidden", "NotFound", "Conflict", "ValidationError",
                        "InternalError");
    }
}
