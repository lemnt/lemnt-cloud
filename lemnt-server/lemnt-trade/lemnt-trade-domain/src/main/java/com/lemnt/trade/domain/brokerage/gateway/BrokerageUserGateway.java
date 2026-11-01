package com.lemnt.trade.domain.brokerage.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.brokerage.model.BrokerageUser;
import com.lemnt.trade.domain.brokerage.model.BrokerageUserFilter;

/** 分销用户存储契约。 */
public interface BrokerageUserGateway {
    BrokerageUser find(Long id);

    /** 锁定分销用户行，调用方持有事务。 */
    BrokerageUser lock(Long id);

    BrokerageUser save(BrokerageUser value);

    /** 清除推广员绑定，updateById 不会写入 null。 */
    boolean clearBind(Long id);

    PageResult<BrokerageUser> page(BrokerageUserFilter filter);

    long countChildren(Long bindUserId);

    /** 一级下级的编号，用于统计二级人数。 */
    java.util.List<Long> childIds(Long bindUserId);
}
