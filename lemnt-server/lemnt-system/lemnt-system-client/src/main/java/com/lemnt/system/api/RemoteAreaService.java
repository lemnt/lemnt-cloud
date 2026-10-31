package com.lemnt.system.api;

import java.util.List;
import java.util.Map;

/** 只读行政区名称契约，复用系统地区资源。 */
public interface RemoteAreaService {

    /** 批量返回省市区全称，未知编号不出现在结果中。 */
    Map<Long, String> getNames(List<Long> areaIds);
}
