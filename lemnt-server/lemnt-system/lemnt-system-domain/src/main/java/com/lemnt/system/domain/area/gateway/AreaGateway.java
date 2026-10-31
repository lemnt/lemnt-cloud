package com.lemnt.system.domain.area.gateway;
import com.lemnt.system.domain.area.model.Area;
import java.util.List;
import java.util.Map;
/** 地区资源读取，不使用业务数据库。 */
public interface AreaGateway {
    /** 返回中国下省市区树。 */
    List<Area> chinaTree();
    /** 批量格式化源资源中的地区全称，未知编号省略。 */
    Map<Long, String> names(List<Long> ids);

}
