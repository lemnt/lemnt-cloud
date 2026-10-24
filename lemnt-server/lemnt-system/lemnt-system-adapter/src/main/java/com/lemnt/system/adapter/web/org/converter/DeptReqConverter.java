package com.lemnt.system.adapter.web.org.converter;

import com.lemnt.system.adapter.web.org.req.DeptAddReq;
import com.lemnt.system.adapter.web.org.req.DeptListReq;
import com.lemnt.system.adapter.web.org.req.DeptUpdateReq;
import com.lemnt.system.application.org.cmd.DeptAddCmd;
import com.lemnt.system.application.org.cmd.DeptUpdateCmd;
import com.lemnt.system.application.org.qry.DeptListQry;

/**
 * 部门请求对象转换器
 *
 * @author wangxx
 */
public class DeptReqConverter {

    public static DeptAddCmd toCmd(DeptAddReq req) {
        return new DeptAddCmd(
                req.parentId(),
                req.deptName(),
                req.orderNum(),
                req.leader(),
                req.phone(),
                req.email(),
                req.status()
        );
    }

    public static DeptUpdateCmd toCmd(DeptUpdateReq req) {
        return new DeptUpdateCmd(
                req.deptId(),
                req.parentId(),
                req.ancestors(),
                req.deptName(),
                req.orderNum(),
                req.leader(),
                req.phone(),
                req.email(),
                req.status()
        );
    }

    public static DeptListQry toQry(DeptListReq item) {
        if (item == null) {
            return null;
        }
        return new DeptListQry(
                item.deptName(),
                item.status()
        );
    }
}
