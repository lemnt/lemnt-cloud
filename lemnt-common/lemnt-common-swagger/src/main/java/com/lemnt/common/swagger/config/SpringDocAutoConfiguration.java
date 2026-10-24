package com.lemnt.common.swagger.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import com.lemnt.common.swagger.config.properties.SpringDocProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Swagger 文档配置
 *
 * @author wangxx
 */
@EnableConfigurationProperties(SpringDocProperties.class)
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
public class SpringDocAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    public OpenAPI openApi(SpringDocProperties properties) {
        Components components = new Components()
                .addSecuritySchemes("bearerAuth", securityScheme())
                .addSchemas("ProblemDetails", problemDetailsSchema())
                .addSchemas("ValidationProblemDetails", validationProblemDetailsSchema());
        addProblemResponse(components, "Unauthorized", "未认证");
        addProblemResponse(components, "Forbidden", "没有访问权限");
        addProblemResponse(components, "NotFound", "资源不存在");
        addProblemResponse(components, "Conflict", "资源状态冲突");
        addProblemResponse(components, "ValidationError", "请求参数校验失败");
        addProblemResponse(components, "InternalError", "服务器内部错误");
        return new OpenAPI().openapi("3.1.0")
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(convertInfo(properties.getInfo()))
                .servers(servers(properties.getGatewayUrl()));
    }

    public SecurityScheme securityScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }

    private Info convertInfo(SpringDocProperties.InfoProperties infoProperties) {
        Info info = new Info();
        info.setTitle(infoProperties.getTitle());
        info.setDescription(infoProperties.getDescription());
        info.setContact(infoProperties.getContact());
        info.setLicense(infoProperties.getLicense());
        info.setVersion(infoProperties.getVersion());
        return info;
    }

    public List<Server> servers(String gatewayUrl) {
        List<Server> serverList = new ArrayList<>();
        serverList.add(new Server().url(gatewayUrl));
        return serverList;
    }

    private Schema<?> problemDetailsSchema() {
        return new ObjectSchema()
                .addProperty("type", new StringSchema().format("uri-reference"))
                .addProperty("title", new StringSchema())
                .addProperty("status", new IntegerSchema().format("int32"))
                .addProperty("detail", new StringSchema())
                .addProperty("instance", new StringSchema().format("uri-reference"))
                .addProperty("code", new StringSchema())
                .addProperty("traceId", new StringSchema())
                .required(List.of("type", "title", "status", "detail", "instance", "code"));
    }

    private Schema<?> validationProblemDetailsSchema() {
        Schema<?> validationError = new ObjectSchema()
                .addProperty("pointer", new StringSchema())
                .addProperty("message", new StringSchema())
                .required(List.of("pointer", "message"));
        return problemDetailsSchema()
                .addProperty("errors", new ArraySchema().items(validationError))
                .required(List.of("type", "title", "status", "detail", "instance", "code", "errors"));
    }

    private void addProblemResponse(Components components, String name, String description) {
        MediaType mediaType = new MediaType().schema(new ObjectSchema().$ref("#/components/schemas/ProblemDetails"));
        components.addResponses(name, new ApiResponse().description(description)
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                        mediaType)));
    }
}
