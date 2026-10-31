package com.lemnt.member.domain.growth.gateway;

import java.util.Collection;

import com.lemnt.member.domain.growth.model.Level;

import java.util.List;

/** member_level 表领域存储网关。 */
public interface MemberLevelGateway {

    /**
     * 查询等级。
     */
    List<Level> levels();

    /** 按名称包含及状态查询等级。 */
    List<Level> levels(String name, Integer status);

    /**
     * 等级。
     */
    Level level(Long id);

    /**
     * 等级保存。
     */
    Long levelSave(Level value);

    /**
     * 等级删除。
     */
    boolean levelDelete(Long id);
    /** 按真实关联ID批量读取，空集合不查表。 */
    List<Level> levelsByIds(Collection<Long> ids);
}
