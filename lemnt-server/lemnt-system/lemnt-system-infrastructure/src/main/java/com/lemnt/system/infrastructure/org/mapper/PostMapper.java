package com.lemnt.system.infrastructure.org.mapper;

import com.lemnt.system.domain.org.model.Post;
import com.lemnt.system.infrastructure.org.dataobject.PostDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Arrays;
import java.util.List;

/**
 * Post 数据层
 *
 * @author wangxx
 */
@Mapper
public interface PostMapper extends BaseMapper<PostDO> {

    /**
     * 分页查询岗位列表
     *
     * @param page 分页参数
     * @param post 查询条件
     * @return 分页结果
     */
    IPage<PostDO> selectPage(Page<PostDO> page, @Param("qry") Post post);

    /**
     * 查询所有岗位
     *
     * @return 岗位列表
     */
    default List<PostDO> selectPostAll() {
        return this.selectList(Wrappers.<PostDO>lambdaQuery()
                .orderByAsc(PostDO::getPostSort));
    }

    /**
     * 根据用户ID获取岗位选择框列表
     *
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    List<Long> selectPostListByUserId(Long userId);

    /**
     * 查询用户所属岗位组
     *
     * @param userName 用户名
     * @return 结果
     */
    List<PostDO> selectPostsByUserName(String userName);

    /**
     * 校验岗位名称
     *
     * @param postName 岗位名称
     * @return 结果
     */
    default PostDO checkPostNameUnique(String postName) {
        return this.selectOne(Wrappers.<PostDO>lambdaQuery()
                .eq(PostDO::getPostName, postName).last("limit 1"));
    }

    /**
     * 校验岗位编码
     *
     * @param postCode 岗位编码
     * @return 结果
     */
    default PostDO checkPostCodeUnique(String postCode) {
        return checkPostCodeUnique(postCode, null);
    }

    /** 按编码检查冲突，不过滤停用岗位；修改时排除自身。 */
    default PostDO checkPostCodeUnique(String postCode, Long excludePostId) {
        return this.selectOne(Wrappers.<PostDO>lambdaQuery()
                .eq(PostDO::getPostCode, postCode)
                .ne(excludePostId != null, PostDO::getPostId, excludePostId)
                .last("limit 1"));
    }
}
