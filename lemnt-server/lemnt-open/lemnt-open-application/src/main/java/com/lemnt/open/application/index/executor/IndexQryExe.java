package com.lemnt.open.application.index.executor;

import com.lemnt.open.application.index.vo.IndexVo;
import org.springframework.stereotype.Component;

/**
 * 首页信息查询
 *
 * @author wangxx
 */
@Component
public class IndexQryExe {

    public IndexVo execute() {
        return new IndexVo("test");
    }
}
