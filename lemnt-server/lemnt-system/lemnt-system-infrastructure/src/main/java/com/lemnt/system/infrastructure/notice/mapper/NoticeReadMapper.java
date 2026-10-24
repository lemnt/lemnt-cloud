package com.lemnt.system.infrastructure.notice.mapper;

import cn.hutool.core.collection.CollUtil;
import com.lemnt.system.domain.notice.model.Notice;
import com.lemnt.system.domain.notice.model.NoticeReadUser;
import com.lemnt.system.infrastructure.notice.dataobject.NoticeDO;
import com.lemnt.system.infrastructure.notice.dataobject.NoticeReadDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * NoticeRead 数据层
 *
 * @author wangxx
 */
@Mapper
public interface NoticeReadMapper extends BaseMapper<NoticeReadDO> {

    /**
     * 主键由数据库生成；重复标记同一用户同一公告时保留首次阅读记录。
     */
    int insertReadRecords(@Param("records") List<NoticeReadDO> records);

    /**
     * 查询某用户未读公告数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    int selectUnreadCount(@Param("userId") Long userId);

    /**
     * 查询某用户是否已读某公告
     *
     * @param noticeId 公告ID
     * @param userId   用户ID
     * @return 已读记录数（0未读 1已读）
     */
    default long selectIsRead(@Param("noticeId") Long noticeId, @Param("userId") Long userId) {
        return selectCount(Wrappers.<NoticeReadDO>lambdaQuery()
                .eq(NoticeReadDO::getNoticeId, noticeId)
                .eq(NoticeReadDO::getUserId, userId));
    }

    /**
     * 查询带已读状态的公告列表（SQL层限制条数，一次查询完成）
     *
     * @param userId 用户ID
     * @param limit  最多返回条数
     * @return 带 isRead 标记的公告列表
     */
    List<Notice> selectNoticeListWithReadStatus(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 查询已阅读某公告的用户列表
     *
     * @param noticeId    公告ID
     * @param searchValue 搜索值
     * @return 已读用户列表
     */
    IPage<NoticeReadUser> selectPageReadUsers(Page<NoticeDO> page, @Param("noticeId") Long noticeId, @Param("searchValue") String searchValue);

    /**
     * 公告删除时清理对应已读记录
     *
     * @param noticeIds 公告ID数组
     * @return 结果
     */
    default int deleteByNoticeIds(List<Long> noticeIds) {
        if (CollUtil.isEmpty(noticeIds)) {
            return 0;
        }
        return delete(Wrappers.<NoticeReadDO>lambdaQuery()
                .in(NoticeReadDO::getNoticeId, noticeIds));
    }
}
