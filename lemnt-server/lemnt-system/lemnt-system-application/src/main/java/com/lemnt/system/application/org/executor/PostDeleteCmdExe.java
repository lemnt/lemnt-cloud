package com.lemnt.system.application.org.executor;

import com.lemnt.system.domain.org.gateway.PostGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 删除岗位命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class PostDeleteCmdExe {
    private final PostGateway postGateway;
    public Boolean execute(List<Long> postIds) {
        return postGateway.deleteByIds(postIds);
    }
}
