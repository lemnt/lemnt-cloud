package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.gateway.CombinationRecordGateway;
import com.lemnt.mall.domain.promotion.model.CombinationRecord;
import com.lemnt.mall.domain.promotion.model.CombinationRecordFilter;
import com.lemnt.mall.infrastructure.promotion.converter.CombinationConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.CombinationRecordDO;
import com.lemnt.mall.infrastructure.promotion.mapper.CombinationRecordMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 拼团记录仅操作记录表，成团人数串行更新依赖行锁。 */
@Component
@RequiredArgsConstructor
public class CombinationRecordGatewayImpl implements CombinationRecordGateway {
    private final CombinationRecordMapper mapper;

    @Override
    public CombinationRecord find(Long id) {
        return CombinationConverter.model(mapper.selectById(id));
    }

    @Override
    public CombinationRecord lockById(Long id) {
        return CombinationConverter.model(mapper.lockById(id));
    }

    @Override
    public CombinationRecord save(CombinationRecord value) {
        var row = CombinationConverter.row(value);
        if (row.getId() == null) { mapper.insert(row); } else { mapper.updateById(row); }
        return CombinationConverter.model(row);
    }

    @Override
    public boolean finish(Long id, Integer status, Integer userCount, Boolean virtualGroup,
            LocalDateTime endTime) {
        return mapper.update(null, Wrappers.<CombinationRecordDO>lambdaUpdate()
                .eq(CombinationRecordDO::getId, id)
                .set(CombinationRecordDO::getStatus, status)
                .set(CombinationRecordDO::getUserCount, userCount)
                .set(CombinationRecordDO::getVirtualGroup, virtualGroup)
                .set(CombinationRecordDO::getEndTime, endTime)) == 1;
    }

    @Override
    public boolean bindOrder(Long id, Long orderId) {
        return mapper.update(null, Wrappers.<CombinationRecordDO>lambdaUpdate()
                .eq(CombinationRecordDO::getId, id).set(CombinationRecordDO::getOrderId, orderId)) == 1;
    }

    @Override
    public PageResult<CombinationRecord> page(CombinationRecordFilter filter) {
        var page = mapper.selectPage(new Page<CombinationRecordDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<CombinationRecordDO>lambdaQuery()
                        .eq(filter.userId() != null, CombinationRecordDO::getUserId, filter.userId())
                        .eq(filter.activityId() != null, CombinationRecordDO::getActivityId, filter.activityId())
                        .eq(filter.status() != null, CombinationRecordDO::getStatus, filter.status())
                        .eq(filter.headId() != null, CombinationRecordDO::getHeadId, filter.headId())
                        .orderByDesc(CombinationRecordDO::getId));
        return PageResult.of(page.getRecords().stream().map(CombinationConverter::model).toList(), page.getTotal());
    }

    @Override
    public List<CombinationRecord> byHeadId(Long headId) {
        return mapper.selectList(Wrappers.<CombinationRecordDO>lambdaQuery()
                        .eq(CombinationRecordDO::getHeadId, headId).orderByAsc(CombinationRecordDO::getId))
                .stream().map(CombinationConverter::model).toList();
    }

    @Override
    public List<CombinationRecord> byUserAndActivity(Long userId, Long activityId) {
        return mapper.selectList(Wrappers.<CombinationRecordDO>lambdaQuery()
                        .eq(CombinationRecordDO::getUserId, userId)
                        .eq(CombinationRecordDO::getActivityId, activityId)
                        .orderByDesc(CombinationRecordDO::getId))
                .stream().map(CombinationConverter::model).toList();
    }

    @Override
    public CombinationRecord inProgressHead(Long headId) {
        return CombinationConverter.model(mapper.selectOne(Wrappers.<CombinationRecordDO>lambdaQuery()
                .eq(CombinationRecordDO::getId, headId)
                .eq(CombinationRecordDO::getHeadId, CombinationRecord.HEAD_ID_GROUP)
                .eq(CombinationRecordDO::getStatus, 0)));
    }

    @Override
    public List<CombinationRecord> expiredHeads(LocalDateTime now) {
        return mapper.selectList(Wrappers.<CombinationRecordDO>lambdaQuery()
                        .eq(CombinationRecordDO::getHeadId, CombinationRecord.HEAD_ID_GROUP)
                        .eq(CombinationRecordDO::getStatus, 0)
                        .lt(CombinationRecordDO::getExpireTime, now)
                        .orderByAsc(CombinationRecordDO::getId))
                .stream().map(CombinationConverter::model).toList();
    }

    @Override
    public List<CombinationRecord> latest(int count) {
        var page = mapper.selectPage(new Page<CombinationRecordDO>(1, count),
                Wrappers.<CombinationRecordDO>lambdaQuery().orderByDesc(CombinationRecordDO::getId));
        return page.getRecords().stream().map(CombinationConverter::model).toList();
    }

    @Override
    public long userCount() {
        return mapper.selectCount(Wrappers.<CombinationRecordDO>lambdaQuery());
    }

    @Override
    public long successCount(Long activityId) {
        return mapper.selectCount(Wrappers.<CombinationRecordDO>lambdaQuery()
                .eq(CombinationRecordDO::getActivityId, activityId)
                .eq(CombinationRecordDO::getHeadId, CombinationRecord.HEAD_ID_GROUP)
                .eq(CombinationRecordDO::getStatus, 1));
    }
}
