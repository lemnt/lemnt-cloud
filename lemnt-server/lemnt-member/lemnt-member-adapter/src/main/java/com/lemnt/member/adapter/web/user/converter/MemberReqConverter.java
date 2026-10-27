package com.lemnt.member.adapter.web.user.converter;

import com.lemnt.member.adapter.web.growth.req.MemberCatalogQueryGroupsPageReq;
import com.lemnt.member.application.user.qry.GroupPageQry;
import com.lemnt.member.adapter.web.growth.req.MemberCatalogQueryTagsPageReq;
import com.lemnt.member.application.user.qry.TagPageQry;

/** 会员分类分页请求转换器。 */
public final class MemberReqConverter {

    private MemberReqConverter() {}

    /** 转换分组分页请求。 */
    public static GroupPageQry toGroupPageQry(MemberCatalogQueryGroupsPageReq req) {
        return new GroupPageQry(req.name(), req.status(),
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }

    /** 转换标签分页请求。 */
    public static TagPageQry toTagPageQry(MemberCatalogQueryTagsPageReq req) {
        return new TagPageQry(req.name(),
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }
}
