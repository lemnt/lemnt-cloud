package com.lemnt.member.domain.address.gateway;

import java.util.List;
import java.util.Map;

/** 地址展示所需的地区名称读取边界。 */
public interface MemberAddressAreaGateway {

    /** 按去重编号批量获取已知地区全称，未知或暂不可用时省略。 */
    Map<Long, String> names(List<Long> areaIds);
}
