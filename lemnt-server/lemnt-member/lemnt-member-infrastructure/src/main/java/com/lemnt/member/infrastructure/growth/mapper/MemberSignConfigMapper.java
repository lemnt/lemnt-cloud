package com.lemnt.member.infrastructure.growth.mapper;

import com.lemnt.member.infrastructure.growth.dataobject.SignConfigDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** member_sign_config 表数据访问接口。 */
@Mapper
public interface MemberSignConfigMapper extends BaseMapper<SignConfigDO> {

    /**
     * 签到配置项。
     */
    default List<SignConfigDO> signConfigs() {
        return this.selectList(Wrappers.<SignConfigDO>lambdaQuery()
                .orderByAsc(SignConfigDO::getSigninDay));
    }

    /**
     * 签到配置保存。
     */
    @Insert(
            "INSERT INTO member_sign_config(signin_day,point,experience,status)"
            + " VALUES(#{signinDay},#{point},#{experience},#{status}) ON DUPLICATE KEY UPDATE"
            + " point=#{point},experience=#{experience},status=#{status}")
    int signConfigSave(SignConfigDO value);

    /**
     * 签到配置删除。
     */
    default int signConfigDelete(Integer day) {
        return this.delete(Wrappers.<SignConfigDO>lambdaQuery()
                .eq(SignConfigDO::getSigninDay, day));
    }
}
