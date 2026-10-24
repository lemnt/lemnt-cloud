package com.lemnt.system.application.org.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.application.org.cmd.PostAddCmd;
import com.lemnt.system.domain.org.model.Post;
import com.lemnt.system.domain.org.gateway.PostGateway;

/**
 * 新增岗位命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class PostAddCmdExe {
    private final PostGateway postGateway;

    public Boolean execute(PostAddCmd cmd) {
        // 停用岗位也占用编码，重复时禁止写入。
        if (postGateway.checkPostCodeUnique(cmd.postCode()) != null) {
            throw new ServiceException("岗位编码已存在");
        }
        Post entity = new Post();
        entity.setPostName(cmd.postName());
        entity.setPostCode(cmd.postCode());
        entity.setPostSort(cmd.postSort());
        entity.setStatus(cmd.status());
        entity.setRemark(cmd.remark());
        entity.setCreateBy(SecurityUtils.getUsername());
        return postGateway.insert(entity);
    }
}
