package com.lemnt.member.infrastructure.growth.gateway;

import java.util.Collection;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.growth.gateway.MemberLevelGateway;
import com.lemnt.member.domain.growth.model.Level;
import com.lemnt.member.infrastructure.growth.converter.LevelConverter;
import com.lemnt.member.infrastructure.growth.mapper.MemberLevelMapper;
import com.lemnt.member.infrastructure.growth.dataobject.LevelDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** member_level 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class MemberLevelGatewayImpl implements MemberLevelGateway {

    private final MemberLevelMapper memberLevelMapper;

    /**
     * 查询等级。
     */
    @Override
    public List<Level> levels() {
        return levels(null, null);
    }

    /** 管理筛选和简单列表使用同一Lambda条件，按等级升序。 */
    @Override
    public List<Level> levels(String name, Integer status) {
        return memberLevelMapper.selectList(Wrappers.<LevelDO>lambdaQuery()
                .like(name != null, LevelDO::getName, name)
                .eq(status != null, LevelDO::getStatus, status)
                .orderByAsc(LevelDO::getLevel)).stream().map(LevelConverter::toEntity).toList();
    }

    /**
     * 等级。
     */
    @Override
    public Level level(Long id) {
        return LevelConverter.toEntity(memberLevelMapper.selectById(id));
    }

    /**
     * 等级保存。
     */
    @Override
    public Long levelSave(Level value) {
        var row = LevelConverter.toDO(value);
        if (value.id() == null) {
            memberLevelMapper.insert(row);
        }
        else if (memberLevelMapper.updateById(row) != 1) {
            throw new ServiceException("记录不存在", 409);
        }
        return row.getId();
    }

    /**
     * 等级删除。
     */
    @Override
    public boolean levelDelete(Long id) {
        return memberLevelMapper.levelDelete(id) == 1;
    }
    /** 批量关联返显，避免逐会员查询。 */
    @Override
    public List<Level> levelsByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return memberLevelMapper.selectList(Wrappers.<LevelDO>lambdaQuery().in(LevelDO::getId, ids))
                .stream().map(LevelConverter::toEntity).toList();
    }
}
