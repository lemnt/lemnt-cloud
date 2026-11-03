package com.lemnt.order.domain.event.gateway;

/** 消费凭证与业务效果共享本库事务；业务抛错时凭证一并回滚。 */
public interface MessageInboxGateway {
    /** 持有稳定业务锚点后校验前置凭证，必须在本地事务内。 */
    boolean has(String consumer,String type,String businessKey);
    /** 首次返回true；同号同内容重投返回false；同号不同内容拒绝。 */
    boolean begin(String consumer, String eventNo, String type, String businessKey, String payload);
}
