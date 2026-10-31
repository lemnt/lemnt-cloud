package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.gateway.BargainRecordGateway;
import com.lemnt.mall.domain.promotion.model.BargainRecord;
import com.lemnt.mall.domain.promotion.model.BargainRecordFilter;
import com.lemnt.mall.infrastructure.promotion.converter.BargainConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.BargainRecordDO;
import com.lemnt.mall.infrastructure.promotion.mapper.BargainRecordMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 砍价记录仅操作记录表，助力金额按旧价格条件更新避免并发覆盖。 */
@Component
@RequiredArgsConstructor
public class BargainRecordGatewayImpl implements BargainRecordGateway {
    private final BargainRecordMapper mapper;

    @Override
    public BargainRecord find(Long id) {
        return BargainConverter.model(mapper.selectById(id));
    }

    @Override
    public BargainRecord save(BargainRecord value) {
        var row = BargainConverter.row(value);
        if (row.getId() == null) { mapper.insert(row); } else { mapper.updateById(row); }
        return BargainConverter.model(row);
    }

    @Override
    public boolean updatePrice(Long id, BigDecimal expected, BigDecimal price, Integer status,
            LocalDateTime endTime) {
        return mapper.update(null, Wrappers.<BargainRecordDO>lambdaUpdate()
                .eq(BargainRecordDO::getId, id)
                .eq(BargainRecordDO::getBargainPrice, expected)
                .set(BargainRecordDO::getBargainPrice, price)
                .set(BargainRecordDO::getStatus, status)
                .set(BargainRecordDO::getEndTime, endTime)) == 1;
    }

    @Override
    public boolean bindOrder(Long id, Long orderId) {
        return mapper.update(null, Wrappers.<BargainRecordDO>lambdaUpdate()
                .eq(BargainRecordDO::getId, id).isNull(BargainRecordDO::getOrderId)
                .set(BargainRecordDO::getOrderId, orderId)) == 1;
    }

    @Override
    public PageResult<BargainRecord> page(BargainRecordFilter filter) {
        var page = mapper.selectPage(new Page<BargainRecordDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<BargainRecordDO>lambdaQuery()
                        .eq(filter.userId() != null, BargainRecordDO::getUserId, filter.userId())
                        .eq(filter.activityId() != null, BargainRecordDO::getActivityId, filter.activityId())
                        .eq(filter.status() != null, BargainRecordDO::getStatus, filter.status())
                        .orderByDesc(BargainRecordDO::getId));
        return PageResult.of(page.getRecords().stream().map(BargainConverter::model).toList(), page.getTotal());
    }

    @Override
    public List<BargainRecord> byUserAndActivity(Long userId, Long activityId) {
        return mapper.selectList(Wrappers.<BargainRecordDO>lambdaQuery()
                        .eq(BargainRecordDO::getUserId, userId)
                        .eq(BargainRecordDO::getActivityId, activityId)
                        .orderByDesc(BargainRecordDO::getId))
                .stream().map(BargainConverter::model).toList();
    }

    @Override
    public long countByUserAndActivityAndStatus(Long userId, Long activityId, Integer status) {
        return mapper.selectCount(Wrappers.<BargainRecordDO>lambdaQuery()
                .eq(BargainRecordDO::getUserId, userId)
                .eq(BargainRecordDO::getActivityId, activityId)
                .eq(BargainRecordDO::getStatus, status));
    }

    @Override
    public List<BargainRecord> latest(Long activityId, Integer status, int count) {
        var page = mapper.selectPage(new Page<BargainRecordDO>(1, count),
                Wrappers.<BargainRecordDO>lambdaQuery()
                        .eq(activityId != null, BargainRecordDO::getActivityId, activityId)
                        .eq(status != null, BargainRecordDO::getStatus, status)
                        .orderByDesc(BargainRecordDO::getId));
        return page.getRecords().stream().map(BargainConverter::model).toList();
    }

    @Override
    public long countByStatus(Integer status) {
        return mapper.selectCount(Wrappers.<BargainRecordDO>lambdaQuery()
                .eq(status != null, BargainRecordDO::getStatus, status));
    }

    @Override
    public List<BargainRecord> byActivityAndStatus(Long activityId, Integer status) {
        return mapper.selectList(Wrappers.<BargainRecordDO>lambdaQuery()
                        .eq(BargainRecordDO::getActivityId, activityId)
                        .eq(BargainRecordDO::getStatus, status))
                .stream().map(BargainConverter::model).toList();
    }

    @Override
    public long userCount(Long activityId, Integer status) {
        return mapper.selectCount(Wrappers.<BargainRecordDO>lambdaQuery()
                .eq(activityId != null, BargainRecordDO::getActivityId, activityId)
                .eq(status != null, BargainRecordDO::getStatus, status));
    }
}
