package com.lemnt.system.infrastructure.auth.mapper;

import com.lemnt.common.datascope.annotation.DataScope;
import com.lemnt.system.domain.auth.model.User;
import com.lemnt.system.infrastructure.auth.dataobject.UserDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * User 数据层
 *
 * @author wangxx
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
    /**
     * 根据条件分页查询用户列表
     *
     * @param page 分页参数
     * @param user 用户信息
     * @return 分页结果
     */
    @DataScope(deptAlias = "d", userAlias = "u")
    IPage<User> selectPage(Page<User> page, @Param("qry") User user);

    /**
     * 根据条件查询已配用户角色列表
     *
     * @param user 用户信息
     * @return 用户列表
     */
    @DataScope(deptAlias = "d", userAlias = "u")
    IPage<UserDO> selectAllocatedList(Page<UserDO> page, @Param("qry") User user);

    /**
     * 根据条件查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户列表
     */
    @DataScope(deptAlias = "d", userAlias = "u")
    IPage<UserDO> selectUnallocatedList(Page<UserDO> page, @Param("qry") User user);

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户信息
     */
    User selectUserByUserName(String userName);

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    User selectUserById(Long userId);
}
