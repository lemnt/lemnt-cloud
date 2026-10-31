package com.lemnt.member.infrastructure.user.mapper;

import com.lemnt.member.infrastructure.user.dataobject.MemberTagLinkDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** member_tag_link 表数据访问接口。 */
@Mapper
public interface MemberTagLinkMapper extends BaseMapper<MemberTagLinkDO> {

    /**
     * 读取或替换会员关联的标签编号。
     */
    default List<Long> memberTags(Long memberId) {
        return this.selectList(Wrappers.<MemberTagLinkDO>lambdaQuery()
                .select(MemberTagLinkDO::getTagId)
                .eq(MemberTagLinkDO::getMemberId, memberId)
                .orderByAsc(MemberTagLinkDO::getTagId))
                .stream().map(MemberTagLinkDO::getTagId).toList();
    }

    /**
     * 清除查询标签。
     */
    default int clearTags(Long memberId) {
        return this.delete(Wrappers.<MemberTagLinkDO>lambdaQuery()
                .eq(MemberTagLinkDO::getMemberId, memberId));
    }

    /**
     * 新增标签。
     */
    default int addTag(Long memberId, Long tagId) {
        return this.insert(new MemberTagLinkDO().setMemberId(memberId).setTagId(tagId));
    }
}
