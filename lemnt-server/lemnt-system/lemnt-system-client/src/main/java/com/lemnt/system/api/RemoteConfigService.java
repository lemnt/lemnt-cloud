package com.lemnt.system.api;

import com.lemnt.system.api.bo.BusinessConfigSaveBo;

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

    /** 保存完整业务配置组，由调用域先完成业务校验，system单次原子落库。 */
    void saveBusinessConfig(BusinessConfigSaveBo value);
}
