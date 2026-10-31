package com.lemnt.system.application.dubbo;

import com.lemnt.system.api.RemoteAreaService;
import com.lemnt.system.domain.area.gateway.AreaGateway;
import org.apache.dubbo.config.annotation.DubboService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;

/** 地区名称远程读取，数据来自现有系统地区资源。 */
@DubboService
@RequiredArgsConstructor
public class RemoteAreaServiceImpl implements RemoteAreaService {

    private final AreaGateway areaGateway;

    /** 一次批量读取全称，空输入无需遍历地区资源。 */
    @Override
    public Map<Long, String> getNames(List<Long> areaIds) {
        return areaIds == null || areaIds.isEmpty() ? Map.of() : areaGateway.names(areaIds);
    }
}
