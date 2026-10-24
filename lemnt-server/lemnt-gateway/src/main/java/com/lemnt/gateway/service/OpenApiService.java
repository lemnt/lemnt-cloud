package com.lemnt.gateway.service;

import com.lemnt.gateway.model.OpenApiClient;

import java.util.Map;

public interface OpenApiService {

    OpenApiClient getByClientId(String clientId);

    /**
     * 校验时间戳是否有效
     *
     * @param timestamp 时间戳
     */
    void checkTimestamp(String timestamp);

    /**
     * 校验nonce
     *
     * @param clientId 客户端ID
     * @param nonce    nonce
     */
    void checkNonce(String clientId, String nonce);

    /**
     * 校验签名
     *
     * @param clientId  客户端ID
     * @param timestamp 时间戳
     * @param nonce     nonce
     * @param appSecret 应用密钥
     * @param sign      签名
     */
    void verifySign(String clientId, String timestamp, String nonce, String appSecret, String sign, Map<String, Object> params);
}
