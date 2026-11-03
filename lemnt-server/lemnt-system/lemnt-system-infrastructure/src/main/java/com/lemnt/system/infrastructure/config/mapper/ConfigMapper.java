package com.lemnt.system.infrastructure.config.mapper;

import com.lemnt.system.domain.config.model.Config;
import com.lemnt.system.infrastructure.config.dataobject.ConfigDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Config 数据层
 *
 * @author wangxx
 */
@Mapper
public interface ConfigMapper extends BaseMapper<ConfigDO> {
    /** 唯一键冲突时更新整组，创建审计保持不变。 */
    void saveBusinessConfig(@Param("value") Config value);

    /**
     * 分页查询参数配置列表
     *
     * @param page   分页参数
     * @param config 查询条件
     * @return 分页结果
     */
    IPage<ConfigDO> selectPage(Page<ConfigDO> page, @Param("qry") Config config);
}
