package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.gateway.BannerGateway;
import com.lemnt.mall.domain.promotion.model.Banner;
import com.lemnt.mall.domain.promotion.model.BannerFilter;
import com.lemnt.mall.infrastructure.promotion.converter.BannerConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.BannerDO;
import com.lemnt.mall.infrastructure.promotion.mapper.BannerMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** Banner 仅操作对应表。 */
@Component
@RequiredArgsConstructor
public class BannerGatewayImpl implements BannerGateway {
    private final BannerMapper mapper;

    @Override
    public Banner find(Long id) {
        return BannerConverter.model(mapper.selectById(id));
    }

    @Override
    public Banner save(Banner value) {
        var row = BannerConverter.row(value);
        if (row.getId() == null) {
            mapper.insert(row);
        } else {
            mapper.updateById(row);
        }
        return BannerConverter.model(row);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public PageResult<Banner> page(BannerFilter filter) {
        var page =
                mapper.selectPage(
                        new Page<BannerDO>(filter.pageNum(), filter.pageSize()),
                        Wrappers.<BannerDO>lambdaQuery()
                                .like(
                                        filter.title() != null && !filter.title().isBlank(),
                                        BannerDO::getTitle,
                                        filter.title())
                                .eq(filter.status() != null, BannerDO::getStatus, filter.status())
                                .orderByDesc(BannerDO::getSort)
                                .orderByDesc(BannerDO::getId));
        return PageResult.of(
                page.getRecords().stream().map(BannerConverter::model).toList(), page.getTotal());
    }

    @Override
    public List<Banner> enabledByPosition(Integer position) {
        return mapper
                .selectList(
                        Wrappers.<BannerDO>lambdaQuery()
                                .eq(BannerDO::getStatus, 0)
                                .eq(BannerDO::getPosition, position)
                                .orderByDesc(BannerDO::getSort)
                                .orderByDesc(BannerDO::getId))
                .stream()
                .map(BannerConverter::model)
                .toList();
    }

    @Override
    public boolean increaseBrowseCount(Long id) {
        return mapper.update(
                        null,
                        Wrappers.<BannerDO>lambdaUpdate()
                                .eq(BannerDO::getId, id)
                                .setIncrBy(BannerDO::getBrowseCount, 1))
                == 1;
    }
}
