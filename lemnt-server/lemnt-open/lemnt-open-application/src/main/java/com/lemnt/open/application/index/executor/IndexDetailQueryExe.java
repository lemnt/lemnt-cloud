package com.lemnt.open.application.index.executor;

import com.lemnt.open.application.index.dto.IndexDTO;

import org.springframework.stereotype.Component;

/**
 * 首页信息查询
 *
 * @author wangxx
 */
@Component
public class IndexDetailQueryExe {
    public IndexDTO execute() {
        return new IndexDTO("test");
    }
}
