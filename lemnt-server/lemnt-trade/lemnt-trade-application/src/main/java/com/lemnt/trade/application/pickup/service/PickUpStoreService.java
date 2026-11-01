package com.lemnt.trade.application.pickup.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.pickup.cmd.PickUpBindCmd;
import com.lemnt.trade.application.pickup.cmd.PickUpStoreSaveCmd;
import com.lemnt.trade.application.pickup.dto.AppPickUpStoreDTO;
import com.lemnt.trade.application.pickup.dto.PickUpStoreDTO;
import com.lemnt.trade.application.pickup.qry.PickUpStorePageQry;
import com.lemnt.trade.domain.delivery.gateway.PickUpStoreGateway;
import com.lemnt.trade.domain.delivery.model.PickUpStore;
import com.lemnt.trade.domain.delivery.model.PickUpStoreFilter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;

/** 自提门店管理：营业时间、状态和可核销店员。 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PickUpStoreService {
    private final PickUpStoreGateway stores;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    private PickUpStore store(Long id) {
        var value = stores.find(id);
        require(value != null, "自提门店不存在");
        return value;
    }

    private List<Long> users(List<Long> values) {
        if (values == null) {
            return List.of();
        }
        require(values.size() <= 20 && values.stream().allMatch(id -> id != null && id > 0),
                "核销店员最多20人且编号必须为正");
        require(new HashSet<>(values).size() == values.size(), "核销店员不能重复");
        return List.copyOf(values);
    }

    private void validate(PickUpStoreSaveCmd cmd) {
        require(cmd.name() != null && !cmd.name().isBlank() && cmd.name().length() <= 100,
                "门店名称不能为空且不超过100字");
        require(cmd.introduction() == null || cmd.introduction().length() <= 500, "门店简介不超过500字");
        require(cmd.phone() != null && !cmd.phone().isBlank() && cmd.phone().length() <= 32,
                "门店电话不能为空且不超过32字");
        require(cmd.areaId() != null && cmd.areaId() > 0, "门店地区不合法");
        require(cmd.detailAddress() != null && !cmd.detailAddress().isBlank()
                && cmd.detailAddress().length() <= 255, "门店地址不能为空且不超过255字");
        require(cmd.logo() == null || cmd.logo().length() <= 500, "门店logo不超过500字");
        require(cmd.openingTime() != null && cmd.openingTime().matches("\\d{2}:\\d{2}:\\d{2}")
                && cmd.closingTime() != null && cmd.closingTime().matches("\\d{2}:\\d{2}:\\d{2}")
                && cmd.openingTime().compareTo(cmd.closingTime()) < 0, "营业时间不合法");
        require(cmd.status() != null && (cmd.status() == 0 || cmd.status() == 1),
                "门店状态只能为0开启或1关闭");
        users(cmd.verifyUserIds());
    }

    public Long create(PickUpStoreSaveCmd cmd) {
        require(cmd != null && cmd.id() == null, "门店参数不合法");
        validate(cmd);
        return stores.save(new PickUpStore(null, cmd.name().trim(), cmd.introduction(), cmd.phone().trim(),
                cmd.areaId(), cmd.detailAddress().trim(), cmd.logo(), cmd.openingTime(), cmd.closingTime(),
                cmd.latitude(), cmd.longitude(), users(cmd.verifyUserIds()), cmd.status())).id();
    }

    public Boolean update(PickUpStoreSaveCmd cmd) {
        require(cmd != null && cmd.id() != null && cmd.id() > 0, "门店编号不合法");
        store(cmd.id());
        validate(cmd);
        stores.save(new PickUpStore(cmd.id(), cmd.name().trim(), cmd.introduction(), cmd.phone().trim(),
                cmd.areaId(), cmd.detailAddress().trim(), cmd.logo(), cmd.openingTime(), cmd.closingTime(),
                cmd.latitude(), cmd.longitude(), users(cmd.verifyUserIds()), cmd.status()));
        return true;
    }

    public Boolean bind(PickUpBindCmd cmd) {
        require(cmd != null && cmd.storeId() != null && cmd.storeId() > 0, "门店编号不合法");
        var value = store(cmd.storeId());
        stores.save(new PickUpStore(value.id(), value.name(), value.introduction(), value.phone(),
                value.areaId(), value.detailAddress(), value.logo(), value.openingTime(), value.closingTime(),
                value.latitude(), value.longitude(), users(cmd.verifyUserIds()), value.status()));
        return true;
    }

    public Boolean delete(Long id) {
        require(id != null && id > 0, "门店编号不合法");
        store(id);
        stores.delete(id);
        return true;
    }

    public PickUpStoreDTO detail(Long id) {
        return dto(store(id));
    }

    public PageResult<PickUpStoreDTO> page(PickUpStorePageQry qry) {
        require(qry != null && qry.pageNum() >= 1 && qry.pageNum() <= 1000000
                && qry.pageSize() >= 1 && qry.pageSize() <= 100, "分页参数不合法");
        var result = stores.page(new PickUpStoreFilter(qry.name(), qry.status(), qry.pageNum(),
                qry.pageSize()));
        return PageResult.of(result.getRecords().stream().map(this::dto).toList(), result.getTotal());
    }

    public List<AppPickUpStoreDTO> appList() {
        return stores.enabled().stream().map(value -> new AppPickUpStoreDTO(value.id(), value.name(),
                value.introduction(), value.phone(), value.areaId(), value.detailAddress(), value.logo(),
                value.openingTime(), value.closingTime(), value.latitude(), value.longitude())).toList();
    }

    private PickUpStoreDTO dto(PickUpStore value) {
        return new PickUpStoreDTO(value.id(), value.name(), value.introduction(), value.phone(), value.areaId(),
                value.detailAddress(), value.logo(), value.openingTime(), value.closingTime(), value.latitude(),
                value.longitude(), value.verifyUserIds(), value.status());
    }
}
