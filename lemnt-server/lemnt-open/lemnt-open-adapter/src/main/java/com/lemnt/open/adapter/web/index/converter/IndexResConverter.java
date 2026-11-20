package com.lemnt.open.adapter.web.index.converter;

import com.lemnt.open.adapter.web.index.res.IndexInfoRes;
import com.lemnt.open.application.index.dto.IndexDTO;

/**
 * 首页响应对象组装器
 *
 * @author wangxx
 */
public class IndexResConverter {
    /**
     * IndexDTO → IndexInfoRes
     */
    public static IndexInfoRes toRes(IndexDTO vo) {
        if (vo == null) {
            return null;
        }
        return new IndexInfoRes(vo.name());
    }
}
