package com.lemnt.member.infrastructure.address.gateway;

import com.lemnt.member.domain.address.gateway.MemberAddressAreaGateway;
import com.lemnt.system.api.RemoteAreaService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

/** 批量复用system地区数据，不复制资源或增加地区存储。 */
@Slf4j
@Component
public class MemberAddressAreaGatewayImpl implements MemberAddressAreaGateway {

    @DubboReference(check = false)
    private RemoteAreaService remoteAreaService;

    /** 地区展示暂不可用时保留地址及编号，不影响地址读取。 */
    @Override
    public Map<Long, String> names(List<Long> areaIds) {
        if (areaIds.isEmpty()) return Map.of();
        try {
            return remoteAreaService.getNames(areaIds);
        } catch (RpcException error) {
            log.warn("地址地区名称读取暂不可用：{}", error.getMessage());
            return Map.of();
        }
    }
}
