package com.lemnt.system.infrastructure.area.gateway;
import com.lemnt.system.domain.area.gateway.AreaGateway;
import com.lemnt.system.domain.area.model.Area;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
/** 启动加载源CSV一次，缓存不可变地区树。 */
@Component
public class AreaGatewayImpl implements AreaGateway {
    private final List<Area> tree;
    private final Map<Long,String> names;
    public AreaGatewayImpl() {
        try (var reader=new BufferedReader(new InputStreamReader(new ClassPathResource("area/area.csv").getInputStream(),StandardCharsets.UTF_8))) {
            var children=new HashMap<Integer,List<Row>>();
            for (var line : reader.lines().skip(1).toList()) {
                if(line.isBlank()) continue;
                var values=line.split(",",-1);
                var row=new Row(Integer.parseInt(values[0]),values[1]);
                children.computeIfAbsent(Integer.parseInt(values[3]), key->new ArrayList<>()).add(row);
            }
            tree=nodes(1,children);
            if(tree.isEmpty()) throw new IllegalStateException("中国地区资源为空");
            var formatted = new HashMap<Long,String>();
            formatNames(nodes(0, children), "", formatted);
            names = Map.copyOf(formatted);
        } catch(IOException | NumberFormatException error) { throw new IllegalStateException("地区资源加载失败",error); }
    }
    private List<Area> nodes(int parent, Map<Integer,List<Row>> children) {
        return children.getOrDefault(parent,List.of()).stream().map(row->new Area(row.id(),row.name(),nodes(row.id(),children))).toList();
    }
    @Override public List<Area> chinaTree() { return tree; }
    /** 国内下级从省开始，国外保留国家，使用与源AreaUtils一致的空格分隔。 */
    private void formatNames(List<Area> nodes, String parent, Map<Long,String> result) {
        for (var node : nodes) {
            String name = parent.isEmpty() ? node.name() : parent + " " + node.name();
            result.put(node.id().longValue(), name);
            formatNames(node.children(), node.id() == 1 ? "" : name, result);
        }
    }
    /** 仅返回实际资源中存在的编号，不把编号本身当作名称。 */
    @Override
    public Map<Long,String> names(List<Long> ids) {
        var result = new HashMap<Long,String>();
        for (Long id : ids) {
            String name = id == null ? null : names.get(id);
            if (name != null) result.put(id, name);
        }
        return Map.copyOf(result);
    }
    private record Row(int id,String name) {}
}
