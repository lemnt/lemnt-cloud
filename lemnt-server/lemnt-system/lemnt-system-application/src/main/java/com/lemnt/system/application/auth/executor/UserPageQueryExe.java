package com.lemnt.system.application.auth.executor;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.application.auth.converter.UserConverter;
import com.lemnt.system.application.auth.dto.UserDTO;
import com.lemnt.system.application.auth.qry.UserPageQry;
import com.lemnt.system.domain.auth.model.User;
import com.lemnt.system.domain.auth.gateway.UserGateway;

/**
 * 用户列表查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserPageQueryExe {
    private final UserGateway userGateway;

    /**
     * 分页查询用户列表
     *
     * @param qry 查询条件
     * @return 分页结果
     */
    public PageResult<UserDTO> execute(UserPageQry qry) {
        User condition = new User();
        condition.setUserName(qry.userName());
        condition.setPhonenumber(qry.phonenumber());
        condition.setStatus(qry.status());
        condition.setDeptId(qry.deptId());
        condition.setBeginTime(qry.beginTime());
        condition.setEndTime(qry.endTime());
        PageResult<User> pageResult = userGateway.selectPage(condition, new PageQry(qry.pageNum(), qry.pageSize()));
        return PageResult.of(UserConverter.toDTOList(pageResult.getRecords()), pageResult.getTotal());
    }
}
