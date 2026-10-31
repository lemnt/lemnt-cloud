package com.lemnt.system.application.area.executor;
import com.lemnt.system.application.area.dto.AreaDTO;
import com.lemnt.system.domain.area.gateway.AreaGateway;
import com.lemnt.system.domain.area.model.Area;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
/** 查询只读中国行政区树。 */
@Component @RequiredArgsConstructor
public class AreaTreeQueryExe {
    private final AreaGateway areas;
    public List<AreaDTO> execute() { return areas.chinaTree().stream().map(this::toDTO).toList(); }
    private AreaDTO toDTO(Area area) { return new AreaDTO(area.id(),area.name(),area.children().stream().map(this::toDTO).toList()); }
}
