package com.lemnt.auth.adapter.web.auth.converter;

import com.lemnt.auth.adapter.web.auth.req.UnLockReq;
import com.lemnt.auth.application.auth.cmd.UnLockCmd;

/**
 * 解锁对象组装器
 *
 * @author wangxx
 */
public class UnLockConverter {

    public static UnLockCmd toCmd(UnLockReq item) {
        if (item == null) {
            return null;
        }
        return new UnLockCmd(item.password());
    }
}
