package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.gateway.BargainHelpGateway;
import com.lemnt.mall.domain.promotion.model.BargainHelp;
import com.lemnt.mall.infrastructure.promotion.converter.BargainConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.BargainHelpDO;
import com.lemnt.mall.infrastructure.promotion.mapper.BargainHelpMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 砍价助力仅操作助力表。 */
@Component
@RequiredArgsConstructor
public class BargainHelpGatewayImpl implements BargainHelpGateway {
    private final BargainHelpMapper mapper;

    @Override
    public BargainHelp save(BargainHelp value) {
        var row = BargainConverter.row(value);
        mapper.insert(row);
        return BargainConverter.model(row);
    }

    @Override
    public BargainHelp byRecordAndUser(Long recordId, Long userId) {
        return BargainConverter.model(mapper.selectOne(Wrappers.<BargainHelpDO>lambdaQuery()
                .eq(BargainHelpDO::getRecordId, recordId)
                .eq(BargainHelpDO::getUserId, userId)));
    }

    @Override
    public List<BargainHelp> byRecordId(Long recordId) {
        return mapper.selectList(Wrappers.<BargainHelpDO>lambdaQuery()
                        .eq(BargainHelpDO::getRecordId, recordId).orderByAsc(BargainHelpDO::getId))
                .stream().map(BargainConverter::model).toList();
    }

    @Override
    public long countByUserAndActivity(Long userId, Long activityId) {
        return mapper.selectCount(Wrappers.<BargainHelpDO>lambdaQuery()
                .eq(BargainHelpDO::getUserId, userId)
                .eq(BargainHelpDO::getActivityId, activityId));
    }

    @Override
    public PageResult<BargainHelp> page(Long recordId, Long userId, Long activityId, int pageNum, int pageSize) {
        var page = mapper.selectPage(new Page<BargainHelpDO>(pageNum, pageSize),
                Wrappers.<BargainHelpDO>lambdaQuery()
                        .eq(recordId != null, BargainHelpDO::getRecordId, recordId)
                        .eq(userId != null, BargainHelpDO::getUserId, userId)
                        .eq(activityId != null, BargainHelpDO::getActivityId, activityId)
                        .orderByDesc(BargainHelpDO::getId));
        return PageResult.of(page.getRecords().stream().map(BargainConverter::model).toList(), page.getTotal());
    }
}
