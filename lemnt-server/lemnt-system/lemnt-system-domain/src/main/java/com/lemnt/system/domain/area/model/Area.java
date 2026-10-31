package com.lemnt.system.domain.area.model;
import java.util.List;
/** 只读行政区节点，数据来自源项目地区资源。 */
public record Area(Integer id, String name, List<Area> children) {
    public Area { children=List.copyOf(children); }
}
