package com.lemnt.system.infrastructure.log.mapper;

import com.lemnt.system.domain.log.model.Logininfor;
import com.lemnt.system.infrastructure.log.dataobject.LogininforDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Logininfor 数据层
 *
 * @author wangxx
 */
@Mapper
public interface LogininforMapper extends BaseMapper<LogininforDO> {

    /**
     * 分页查询系统登录日志
     *
     * @param page       分页参数
     * @param logininfor 访问日志对象
     * @return 分页结果
     */
    IPage<LogininforDO> selectPage(Page<LogininforDO> page, @Param("qry") Logininfor logininfor);

    /**
     * 清空系统登录日志
     *
     * @return 结果
     */
    int cleanLogininfor();
}
