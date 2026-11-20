package com.lemnt.promotion.domain.event.gateway;

/**
 * 消费凭证与业务效果共享本库事务；业务抛错时凭证一并回滚。
 */
public interface MessageInboxGateway {
    /**
     * 持有本地资源锚点锁后读取前置动作，不依靠短TTL缓存。
     */
    boolean has(String consumer, String type, String businessKey);

    /**
     * 首次返回true；同号同内容重投返回false；同号不同内容拒绝。
     */
    boolean begin(String consumer, String eventNo, String type, String businessKey, String payload);
}
