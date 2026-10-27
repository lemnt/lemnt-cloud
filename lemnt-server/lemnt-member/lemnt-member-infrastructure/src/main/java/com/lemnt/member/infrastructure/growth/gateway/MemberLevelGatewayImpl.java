package com.lemnt.member.infrastructure.growth.gateway;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.growth.gateway.MemberLevelGateway;
import com.lemnt.member.domain.growth.model.Level;
import com.lemnt.member.infrastructure.growth.converter.LevelConverter;
import com.lemnt.member.infrastructure.growth.mapper.MemberLevelMapper;

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
        return memberLevelMapper.levels().stream().map(LevelConverter::toEntity).toList();
    }

    /**
     * 等级。
     */
    @Override
    public Level level(Long id) {
        return LevelConverter.toEntity(memberLevelMapper.level(id));
    }

    /**
     * 等级保存。
     */
    @Override
    public Long levelSave(Level value) {
        var row = LevelConverter.toDO(value);
        if (value.id() == null) {
            memberLevelMapper.levelInsert(row);
        }
        else if (memberLevelMapper.levelUpdate(row) != 1) {
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
}
