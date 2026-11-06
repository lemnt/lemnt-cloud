package com.lemnt.order.domain.delivery.model;

/**
 * 自提门店分页条件。
 */
public record PickUpStoreFilter(String name, Integer status, int pageNum, int pageSize) {
}
