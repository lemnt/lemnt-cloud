package com.lemnt.system.infrastructure.notice.mapper;

import com.lemnt.system.domain.notice.model.Notice;
import com.lemnt.system.infrastructure.notice.dataobject.NoticeDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * Notice 数据层
 *
 * @author wangxx
 */
@Mapper
public interface NoticeMapper extends BaseMapper<NoticeDO> {

    /**
     * 分页查询公告列表
     *
     * @param page   分页参数
     * @param notice 查询条件
     * @return 分页结果
     */
    IPage<NoticeDO> selectPage(Page<NoticeDO> page, @Param("qry") Notice notice);
}
