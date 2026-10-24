package com.lemnt.system.application.org.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.application.org.cmd.PostUpdateCmd;
import com.lemnt.system.domain.org.model.Post;
import com.lemnt.system.domain.org.gateway.PostGateway;

/**
 * 修改岗位命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class PostUpdateCmdExe {
    private final PostGateway postGateway;

    public Boolean execute(PostUpdateCmd cmd) {
        // 排除当前岗位，仍拦截其他岗位使用相同编码。
        if (postGateway.checkPostCodeUnique(cmd.postCode(), cmd.postId()) != null) {
            throw new ServiceException("岗位编码已存在");
        }
        Post entity = new Post();
        entity.setPostId(cmd.postId());
        entity.setPostName(cmd.postName());
        entity.setPostCode(cmd.postCode());
        entity.setPostSort(cmd.postSort());
        entity.setStatus(cmd.status());
        entity.setRemark(cmd.remark());
        entity.setUpdateBy(SecurityUtils.getUsername());
        return postGateway.update(entity);
    }
}
