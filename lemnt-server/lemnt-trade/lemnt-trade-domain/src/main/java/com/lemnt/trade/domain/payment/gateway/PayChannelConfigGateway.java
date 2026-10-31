package com.lemnt.trade.domain.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.payment.model.PayChannelConfig;
import com.lemnt.trade.domain.payment.model.PayChannelFilter;

import java.util.List;

/** 支付渠道配置表存储契约。 */
public interface PayChannelConfigGateway {
    /** 分页查询渠道配置。 */
    PageResult<PayChannelConfig> page(PayChannelFilter filter);

    /** 按主键查询。 */
    PayChannelConfig find(Long id);

    /** 查询应用下全部渠道，供选择使用。 */
    List<PayChannelConfig> listByAppId(Long appId);

    /** 同一应用下渠道编码是否已被其他配置占用。 */
    boolean exists(Long appId, String code, Long excludeId);

    /** 新增渠道配置，由数据库分配主键。 */
    PayChannelConfig insert(PayChannelConfig value);

    /** 全量更新允许修改的字段。 */
    boolean update(PayChannelConfig value);

    /** 逻辑删除。 */
    boolean delete(Long id);

    /** 应用是否仍存在渠道配置，便于删除应用前校验引用。 */
    boolean channelReferenced(Long appId);
}
