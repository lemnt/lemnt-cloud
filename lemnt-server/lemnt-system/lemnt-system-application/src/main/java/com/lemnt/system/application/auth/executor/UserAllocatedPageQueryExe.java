package com.lemnt.system.application.auth.executor;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.application.auth.converter.UserConverter;
import com.lemnt.system.application.auth.dto.UserDTO;
import com.lemnt.system.application.auth.qry.UserPageQry;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.model.User;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 查询已分配角色用户执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserAllocatedPageQueryExe {
    private final UserGateway userGateway;
    public PageResult<UserDTO> execute(UserPageQry qry) {
        User condition = new User();
        condition.setUserName(qry.userName());
        condition.setPhonenumber(qry.phonenumber());
        condition.setRoleId(qry.roleId());
        PageResult<User> pageResult = userGateway.selectAllocatedList(
                condition, new PageQry(qry.pageNum(), qry.pageSize()));
        return PageResult.of(UserConverter.toDTOList(pageResult.getRecords()), pageResult.getTotal());
    }

    /**
     * 查询未分配角色的用户列表
     *
     * @param qry 查询条件（需要包含 roleId）
     * @return 用户列表
     */
    public PageResult<UserDTO> executeUnallocated(UserPageQry qry) {
        User condition = new User();
        condition.setUserName(qry.userName());
        condition.setPhonenumber(qry.phonenumber());
        condition.setRoleId(qry.roleId());
        PageResult<User> pageResult = userGateway.selectUnallocatedList(
                condition, new PageQry(qry.pageNum(), qry.pageSize()));
        return PageResult.of(UserConverter.toDTOList(pageResult.getRecords()), pageResult.getTotal());
    }
}
