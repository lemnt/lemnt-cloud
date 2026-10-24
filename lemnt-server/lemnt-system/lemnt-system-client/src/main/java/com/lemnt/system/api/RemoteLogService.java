package com.lemnt.system.api;

import com.lemnt.system.api.domain.bo.LogininforBo;
import com.lemnt.system.api.domain.bo.OperLogBo;

/**
 * 日志服务
 *
 * @author wangxx
 */
public interface RemoteLogService {
    /**
     * 保存系统日志
     *
     * @param bo 日志实体
     * @return 结果
     */
    Boolean saveLog(OperLogBo bo);

    /**
     * 保存访问记录
     *
     * @param bo 访问实体
     * @return 结果
     */
    Boolean saveLogininfor(LogininforBo bo);
}
