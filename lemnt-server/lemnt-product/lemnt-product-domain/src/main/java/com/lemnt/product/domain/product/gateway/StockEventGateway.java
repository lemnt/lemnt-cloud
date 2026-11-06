package com.lemnt.product.domain.product.gateway;

import com.lemnt.product.domain.product.model.ProductStockReservation;

/**
 * 库存预占事实出站端口，由所属聚合声明，MQ实现隔离在基础设施层。
 */
public interface StockEventGateway {
    /**
     * 以RocketMQ事务消息发布预占事实：半消息先行，本地短事务原子完成SKU扣减与预占台账写入，
     * 本地事务提交才允许消息对消费者可见；本地事务未成功（库存不足、并发对手先行或SKU不存在）
     * 返回false或抛出参数异常，半消息一并丢弃。
     * 与调用方事务解耦：预占在本地短事务内独立提交，调用方后续失败必须经取消预占补偿，
     * 不存在“外层回滚自动撤销预占”的隐含语义。
     * broker不可用时直接抛出，预占入口同步失败，不做本地暂存。
     *
     * @param draft RESERVED状态的预占草案
     * @return 本地事务提交且事务消息已确认时为true
     */
    boolean publishReserved(ProductStockReservation draft);
}
