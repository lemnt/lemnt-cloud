package com.lemnt.system.adapter.web.area.res;
import java.util.List;
/** 后台地区树独立响应。 */
public record AreaTreeRes(Integer id, String name, List<AreaTreeRes> children) {}
