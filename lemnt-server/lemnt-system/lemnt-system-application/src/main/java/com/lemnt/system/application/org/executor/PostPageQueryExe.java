package com.lemnt.system.application.org.executor;

import java.util.List;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.application.org.converter.PostConverter;
import com.lemnt.system.application.org.dto.PostDTO;
import com.lemnt.system.application.org.qry.PostPageQry;
import com.lemnt.system.domain.org.model.Post;
import com.lemnt.system.domain.org.gateway.PostGateway;

/**
 * 岗位分页查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class PostPageQueryExe {
    private final PostGateway postGateway;

    public PageResult<PostDTO> execute(PostPageQry qry) {
        Post query = new Post();
        query.setPostCode(qry.postCode());
        query.setPostName(qry.postName());
        query.setStatus(qry.status());
        PageResult<Post> pageResult = postGateway.selectPage(query, new PageQry(qry.pageNum(), qry.pageSize()));
        List<PostDTO> dtoList = PostConverter.toDTOList(pageResult.getRecords());
        return PageResult.of(dtoList, pageResult.getTotal());
    }
}
