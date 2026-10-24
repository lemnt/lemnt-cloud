package com.lemnt.system.api;

/**
 * 基础配置
 *
 * @author wangxx
 */
public interface RemoteConfigService {

    /**
     * 通过configKey 查询配置信息
     *
     * @param configKey 配置key
     * @return 结果
     */
    String getConfig(String configKey);

}
