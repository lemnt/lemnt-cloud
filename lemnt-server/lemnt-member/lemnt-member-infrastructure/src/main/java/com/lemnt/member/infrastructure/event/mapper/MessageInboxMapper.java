package com.lemnt.member.infrastructure.event.mapper;

import com.lemnt.member.infrastructure.event.dataobject.MessageInboxDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通过唯一键串行重复消费，冲突后仍需校验冻结内容。
 */
@Mapper
public interface MessageInboxMapper extends BaseMapper<MessageInboxDO> {
    MessageInboxDO lockEvent(@Param("consumer") String consumer, @Param("eventNo") String eventNo);

    MessageInboxDO lockBusiness(
        @Param("consumer") String consumer,
        @Param("type") String type,
        @Param("key") String key);
}
