package com.lemnt.trade.infrastructure.fulfillment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.fulfillment.gateway.ShipmentGateway;
import com.lemnt.trade.domain.fulfillment.model.Shipment;
import com.lemnt.trade.domain.fulfillment.model.ShipmentFilter;
import com.lemnt.trade.infrastructure.fulfillment.converter.FulfillmentConverter;
import com.lemnt.trade.infrastructure.fulfillment.dataobject.ShipmentDO;
import com.lemnt.trade.infrastructure.fulfillment.mapper.ShipmentMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/** Shipment 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class ShipmentGatewayImpl implements ShipmentGateway {
    private final ShipmentMapper shipmentMapper;

    @Override
    public PageResult<Shipment> page(ShipmentFilter filter) {
        var query =
                Wrappers.lambdaQuery(ShipmentDO.class)
                        .eq(filter.orderNo() != null, ShipmentDO::getOrderNo, filter.orderNo())
                        .eq(
                                filter.carrierCode() != null,
                                ShipmentDO::getCarrierCode,
                                filter.carrierCode())
                        .eq(
                                filter.trackingNo() != null,
                                ShipmentDO::getTrackingNo,
                                filter.trackingNo())
                        .isNull("SHIPPED".equals(filter.status()), ShipmentDO::getReceivedAt)
                        .isNotNull("RECEIVED".equals(filter.status()), ShipmentDO::getReceivedAt)
                        .orderByDesc(ShipmentDO::getShippedAt)
                        .orderByDesc(ShipmentDO::getOrderNo);
        var page =
                shipmentMapper.selectPage(new Page<>(filter.pageNum(), filter.pageSize()), query);
        return PageResult.of(
                page.getRecords().stream().map(FulfillmentConverter::shipment).toList(),
                page.getTotal());
    }

    @Override
    /**
     * 发货。
     */
    public Shipment shipment(String no) {
        return FulfillmentConverter.shipment(shipmentRow(no));
    }

    @Override
    /**
     * 保存发货。
     */
    public void saveShipment(Shipment value) {
        insertShipmentRow(FulfillmentConverter.shipment(value));
    }

    @Override
    /**
     * 接收。
     */
    public boolean receive(String no, LocalDateTime time) {
        return shipmentMapper.receive(no, time) == 1;
    }

    @Override
    public java.util.List<String> shippedBefore(LocalDateTime cutoff, int limit) {
        return shipmentMapper
                .selectList(
                        Wrappers.<ShipmentDO>lambdaQuery()
                                .isNull(ShipmentDO::getReceivedAt)
                                .le(ShipmentDO::getShippedAt, cutoff)
                                .orderByAsc(ShipmentDO::getShippedAt)
                                .last("LIMIT " + Math.max(limit, 1)))
                .stream()
                .map(ShipmentDO::getOrderNo)
                .toList();
    }

    /**
     * 发货。
     */
    private ShipmentDO shipmentRow(String orderNo) {
        return shipmentMapper.selectOne(
                Wrappers.<ShipmentDO>lambdaQuery().eq(ShipmentDO::getOrderNo, orderNo));
    }

    /**
     * 插入发货。
     */
    private int insertShipmentRow(ShipmentDO row) {
        return shipmentMapper.insert(row);
    }
}
