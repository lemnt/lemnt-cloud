package com.lemnt.pay.infrastructure.event.mapper;

import com.lemnt.pay.infrastructure.event.dataobject.MessageOutboxDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 原子登记与租约操作；SQL集中XML，锁查询禁用缓存。
 */
@Mapper
public interface MessageOutboxMapper extends BaseMapper<MessageOutboxDO> {
    int insertIfAbsent(MessageOutboxDO row);

    MessageOutboxDO lockBusiness(@Param("type") String type, @Param("key") String key);

    List<MessageOutboxDO> lockDue(@Param("size") int size);

    int claim(@Param("id") long id, @Param("version") long version);

    int sent(@Param("id") long id, @Param("version") long version);

    int failed(
        @Param("id") long id,
        @Param("version") long version,
        @Param("status") String status,
        @Param("delay") int delay,
        @Param("error") String error);

    int retryFailed(@Param("eventNo") String eventNo);
}
