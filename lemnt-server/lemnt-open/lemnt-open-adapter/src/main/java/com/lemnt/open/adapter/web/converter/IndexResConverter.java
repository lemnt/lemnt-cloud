package com.lemnt.open.adapter.web.converter;

import com.lemnt.open.adapter.web.res.IndexInfoRes;
import com.lemnt.open.application.index.vo.IndexVo;

/**
 * 首页响应对象组装器
 *
 * @author wangxx
 */
public class IndexResConverter {

    /**
     * IndexVo → IndexInfoRes
     */
    public static IndexInfoRes toRes(IndexVo vo) {
        if (vo == null) {
            return null;
        }
        return new IndexInfoRes(vo.name());
    }
}
