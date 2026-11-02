package com.lemnt.system.api.bo;

import java.io.Serializable;

/** 内部配置组保存契约：整组JSON和业务操作人，不传数据库实体。 */
public record BusinessConfigSaveBo(String configKey, String configValue, String operator) implements Serializable { }
