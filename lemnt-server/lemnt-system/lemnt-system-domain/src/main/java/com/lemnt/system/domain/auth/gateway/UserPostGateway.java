package com.lemnt.system.domain.auth.gateway;

import java.util.List;

/** UserPost 存储接口。 */
public interface UserPostGateway {
    /**
     * 新增用户岗位关联
     *
     * @param userId  用户ID
     * @param postIds 岗位ID数组
     */
    void insertUserPosts(Long userId, List<Long> postIds);

    /**
     * 删除用户岗位关联
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteUserPostByUserId(Long userId);

    /**
     * 批量删除用户岗位关联
     *
     * @param userIds 用户ID数组
     * @return 是否成功
     */
    Boolean deleteUserPostByUserIds(List<Long> userIds);

    /**
     * 查询岗位被分配的用户数
     *
     * @param postId 岗位ID
     * @return 结果
     */
    Long countUserPostById(Long postId);
}
