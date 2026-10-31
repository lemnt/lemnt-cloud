package com.lemnt.system.infrastructure.org.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.org.gateway.PostGateway;
import com.lemnt.system.domain.org.model.Post;
import com.lemnt.system.infrastructure.org.converter.PostConverter;
import com.lemnt.system.infrastructure.org.dataobject.PostDO;
import com.lemnt.system.infrastructure.org.mapper.PostMapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 岗位网关实现 — infrastructure 层适配器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class PostGatewayImpl implements PostGateway {
    private final PostMapper postMapper;

    @Override
    public Post selectById(Long postId) {
        return PostConverter.toEntity(postMapper.selectById(postId));
    }

    @Override
    public PageResult<Post> selectPage(Post query, PageQry pageQry) {
        Page<PostDO> page = new Page<>(pageQry.getPageNum(), pageQry.getPageSize());
        IPage<PostDO> result = postMapper.selectPage(page, query);
        List<Post> records = PostConverter.toEntityList(result.getRecords());
        return PageResult.of(records, result.getTotal());
    }

    @Override
    public List<Long> selectPostsByUserId(Long userId) {
        return postMapper.selectPostListByUserId(userId);
    }

    @Override
    public List<Post> selectPostAll() {
        return PostConverter.toEntityList(postMapper.selectPostAll());
    }

    @Override
    public Boolean insert(Post entity) {
        return postMapper.insert(PostConverter.toDO(entity)) > 0;
    }

    @Override
    public Boolean update(Post entity) {
        return postMapper.updateById(PostConverter.toDO(entity)) > 0;
    }

    @Override
    public Boolean deleteByIds(List<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return false;
        }
        return postMapper.deleteByIds(postIds) > 0;
    }

    @Override
    public Post checkPostNameUnique(String postName) {
        return PostConverter.toEntity(postMapper.checkPostNameUnique(postName));
    }

    @Override
    public Post checkPostCodeUnique(String postCode) {
        return PostConverter.toEntity(postMapper.checkPostCodeUnique(postCode));
    }

    @Override
    public Post checkPostCodeUnique(String postCode, Long excludePostId) {
        return PostConverter.toEntity(postMapper.checkPostCodeUnique(postCode, excludePostId));
    }
}
