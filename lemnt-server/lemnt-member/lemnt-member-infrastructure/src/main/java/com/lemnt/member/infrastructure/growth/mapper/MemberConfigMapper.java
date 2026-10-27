package com.lemnt.member.infrastructure.growth.mapper;

import com.lemnt.member.infrastructure.growth.dataobject.ConfigDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/** member_config 表数据访问接口。 */
@Mapper
public interface MemberConfigMapper extends BaseMapper<ConfigDO> {

    /**
     * 锁定会员配置行，串行化目录相关变更。
     */
    @Select("SELECT id FROM member_config WHERE id=1 FOR UPDATE")
    Integer lockCatalog();

    /**
     * 配置。
     */
    default ConfigDO config() {
        return this.selectById(1);
    }

    /**
     * 配置保存。
     */
    default int configSave(ConfigDO value) {
        return this.update(null, Wrappers.<ConfigDO>lambdaUpdate()
                .set(ConfigDO::getPointTradeDeductEnable, value.getPointTradeDeductEnable())
                .set(ConfigDO::getPointTradeDeductUnitPrice, value.getPointTradeDeductUnitPrice())
                .set(ConfigDO::getPointTradeDeductMaxPrice, value.getPointTradeDeductMaxPrice())
                .set(ConfigDO::getPointTradeGivePoint, value.getPointTradeGivePoint())
                .eq(ConfigDO::getId, 1));
    }
}
