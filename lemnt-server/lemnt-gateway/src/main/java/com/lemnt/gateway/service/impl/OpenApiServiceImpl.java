package com.lemnt.gateway.service.impl;

import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.redis.service.RedisListService;
import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.gateway.model.OpenApiClient;
import com.lemnt.gateway.service.OpenApiService;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OpenApiServiceImpl implements OpenApiService {

    private static final HmacAlgorithms SIGN_ALGORITHM = HmacAlgorithms.HMAC_SHA_256;

    @Autowired
    private RedisListService redisListService;
    @Autowired
    private RedisStringService redisStringService;

    @Override
    public OpenApiClient getByClientId(String clientId) {
        List<OpenApiClient> accountList = redisListService.lRangeObj(CacheConstants.CLIENT_ACCOUNT_KEY, 0, -1, OpenApiClient.class);
        return accountList.stream().filter(f -> Objects.equals(clientId, f.getClientId()))
                .findFirst().orElse(null);
    }

    @Override
    public void checkTimestamp(String timestamp) {
        long reqTime;
        try {
            reqTime = Long.parseLong(timestamp);
        } catch (Exception e) {
            throw new ServiceException("时间戳格式错误");
        }
        long now = System.currentTimeMillis();
        if (Math.abs(now - reqTime) > 5 * 60 * 1000) {
            throw new ServiceException("请求已过期");
        }
    }

    @Override
    public void checkNonce(String clientId, String nonce) {
        String key = "open:nonce:" + clientId + ":" + nonce;
        Boolean exists = redisStringService.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            throw new ServiceException("重复请求");
        }
        // 保存5分钟
        redisStringService.set(key, "1", 5, TimeUnit.MINUTES);
    }

    @Override
    public void verifySign(String clientId, String timestamp, String nonce, String appSecret, String sign, Map<String, Object> params) {
        // 参数排序
        Map<String, Object> sortParams = new TreeMap<>(params);
        String paramStr = sortParams.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + Objects.toString(entry.getValue(), ""))
                .collect(Collectors.joining("&"));
        String data = String.join("\n", clientId, timestamp, nonce, paramStr);
        String serverSign = new HmacUtils(SIGN_ALGORITHM, appSecret).hmacHex(data);
        if (!MessageDigest.isEqual(serverSign.getBytes(StandardCharsets.UTF_8), sign.getBytes(StandardCharsets.UTF_8))) {
            throw new ServiceException("签名错误");
        }
    }
}
