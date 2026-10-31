package com.lemnt.member.domain.growth.gateway;

import com.lemnt.member.domain.growth.model.LevelRecord;
import com.lemnt.member.domain.growth.model.LevelRecordPageFilter;

import java.util.List;

/** member_level_record 表领域存储网关。 */
public interface MemberLevelRecordGateway {

    /**
     * 等级记录。
     */
    void levelRecord(LevelRecord value);

    /**
     * 等级记录。
     */
    List<LevelRecord> levelRecords(Long memberId, int offset, int limit);

    /**
     * 等级记录数量。
     */
    long levelRecordCount(Long memberId);

    /**
     * 查询等级。
     */
    List<LevelRecord> levels(LevelRecordPageFilter filter);

    /**
     * 查询等级数量。
     */
    long levelsCount(LevelRecordPageFilter filter);

    /**
     * 查询等级详情。
     */
    LevelRecord levelsDetail(Long id);
}
