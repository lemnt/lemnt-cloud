package com.lemnt.system.infrastructure.org.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.org.gateway.PostGateway;
import com.lemnt.system.domain.org.model.Post;
import com.lemnt.system.infrastructure.org.converter.PostConverter;
import com.lemnt.system.infrastructure.org.dataobject.PostDO;
import com.lemnt.system.infrastructure.org.mapper.PostMapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
        return PostConverter.toEntityList(selectPostAllRow());
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
        return PostConverter.toEntity(checkPostNameUniqueRow(postName));
    }

    @Override
    public Post checkPostCodeUnique(String postCode) {
        return PostConverter.toEntity(checkPostCodeUniqueRow(postCode));
    }

    @Override
    public Post checkPostCodeUnique(String postCode, Long excludePostId) {
        return PostConverter.toEntity(checkPostCodeUniqueRow(postCode, excludePostId));
    }

    /**
     * 查询所有岗位
     *
     * @return 岗位列表
     */
    private List<PostDO> selectPostAllRow() {
        return postMapper.selectList(
                Wrappers.<PostDO>lambdaQuery().orderByAsc(PostDO::getPostSort));
    }

    /**
     * 校验岗位名称
     *
     * @param postName 岗位名称
     * @return 结果
     */
    private PostDO checkPostNameUniqueRow(String postName) {
        return postMapper.selectOne(
                Wrappers.<PostDO>lambdaQuery().eq(PostDO::getPostName, postName).last("limit 1"));
    }

    /**
     * 校验岗位编码
     *
     * @param postCode 岗位编码
     * @return 结果
     */
    private PostDO checkPostCodeUniqueRow(String postCode) {
        return checkPostCodeUniqueRow(postCode, null);
    }

    /** 按编码检查冲突，不过滤停用岗位；修改时排除自身。 */
    private PostDO checkPostCodeUniqueRow(String postCode, Long excludePostId) {
        return postMapper.selectOne(
                Wrappers.<PostDO>lambdaQuery()
                        .eq(PostDO::getPostCode, postCode)
                        .ne(excludePostId != null, PostDO::getPostId, excludePostId)
                        .last("limit 1"));
    }
}
