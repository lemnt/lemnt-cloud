package com.lemnt.system.domain.area.gateway;
import com.lemnt.system.domain.area.model.Area;
import java.util.List;
/** 地区资源读取，不使用业务数据库。 */
public interface AreaGateway {
    /** 返回中国下省市区树。 */
    List<Area> chinaTree();
}
