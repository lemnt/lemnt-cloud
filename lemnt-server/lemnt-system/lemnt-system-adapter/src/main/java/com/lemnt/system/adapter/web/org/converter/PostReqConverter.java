package com.lemnt.system.adapter.web.org.converter;

import com.lemnt.system.adapter.web.org.req.PostAddReq;
import com.lemnt.system.adapter.web.org.req.PostPageReq;
import com.lemnt.system.adapter.web.org.req.PostUpdateReq;
import com.lemnt.system.application.org.cmd.PostAddCmd;
import com.lemnt.system.application.org.cmd.PostUpdateCmd;
import com.lemnt.system.application.org.qry.PostPageQry;

/**
 * 岗位请求对象转换器
 *
 * @author wangxx
 */
public class PostReqConverter {

    public static PostAddCmd toCmd(PostAddReq req) {
        return new PostAddCmd(
                req.postName(),
                req.postCode(),
                req.postSort(),
                req.status(),
                req.remark()
        );
    }

    public static PostUpdateCmd toCmd(PostUpdateReq req) {
        return new PostUpdateCmd(
                req.postId(),
                req.postName(),
                req.postCode(),
                req.postSort(),
                req.status(),
                req.remark()
        );
    }

    public static PostPageQry toQry(PostPageReq item) {
        if (item == null) {
            return null;
        }
        return new PostPageQry(
                item.postCode(),
                item.postName(),
                item.status(),
                item.pageNum(),
                item.pageSize(),
                item.beginTime(),
                item.endTime()
        );
    }
}
