package com.lemnt.promotion.infrastructure.promotion.mapper;

import com.lemnt.promotion.infrastructure.promotion.dataobject.KefuConversationDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客服会话表 CRUD。
 */
@Mapper
public interface KefuConversationMapper extends BaseMapper<KefuConversationDO> {
}
