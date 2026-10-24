package com.lemnt.system.application.org.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.application.org.converter.PostConverter;
import com.lemnt.system.application.org.dto.PostDTO;
import com.lemnt.system.domain.org.gateway.PostGateway;

/**
 * 岗位详情查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class PostDetailQueryExe {
    private final PostGateway postGateway;

    public PostDTO execute(Long postId) {
        return PostConverter.toDTO(postGateway.selectById(postId));
    }
}
