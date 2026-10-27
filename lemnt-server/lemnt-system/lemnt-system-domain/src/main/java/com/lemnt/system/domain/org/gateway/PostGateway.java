package com.lemnt.system.domain.org.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.org.model.Post;

import java.util.List;

/**
 * 岗位网关 — domain 层端口
 *
 * @author wangxx
 */
public interface PostGateway {
    /**
     * 根据岗位ID查询信息
     *
     * @param postId 岗位ID
     * @return 岗位信息
     */
    Post selectById(Long postId);

    /**
     * 分页查询岗位列表
     *
     * @param query   查询条件
     * @param pageQry 分页参数
     * @return 分页结果
     */
    PageResult<Post> selectPage(Post query, PageQry pageQry);

    /**
     * 根据用户ID查询岗位列表
     *
     * @param userId 用户ID
     * @return 岗位列表
     */
    List<Long> selectPostsByUserId(Long userId);

    /**
     * 查询所有岗位
     *
     * @return
     */
    List<Post> selectPostAll();

    /**
     * 新增岗位
     *
     * @param entity 岗位信息
     * @return 是否成功
     */
    Boolean insert(Post entity);

    /**
     * 修改岗位
     *
     * @param entity 岗位信息
     * @return 是否成功
     */
    Boolean update(Post entity);

    /**
     * 批量删除岗位
     *
     * @param postIds 岗位ID数组
     * @return 是否成功
     */
    Boolean deleteByIds(List<Long> postIds);

    /**
     * 校验岗位名称是否唯一
     *
     * @param postName 岗位名称
     * @return 岗位信息
     */
    Post checkPostNameUnique(String postName);

    /**
     * 校验岗位编码是否唯一
     *
     * @param postCode 岗位编码
     * @return 岗位信息
     */
    Post checkPostCodeUnique(String postCode);

    /** 校验岗位编码，修改时排除当前岗位，仍检查其他存量重复记录。 */
    Post checkPostCodeUnique(String postCode, Long excludePostId);
}
