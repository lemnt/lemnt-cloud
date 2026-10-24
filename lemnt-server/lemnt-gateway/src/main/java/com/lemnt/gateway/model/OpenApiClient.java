package com.lemnt.gateway.model;

import lombok.Data;

/**
 * OpenApiClient
 */
@Data
public class OpenApiClient {

    /**
     * 应用ID
     */
    private String clientId;

    /**
     * 应用密钥
     */
    private String appSecret;

    /**
     * 临时密钥
     */
    private String interimSecret;
}
