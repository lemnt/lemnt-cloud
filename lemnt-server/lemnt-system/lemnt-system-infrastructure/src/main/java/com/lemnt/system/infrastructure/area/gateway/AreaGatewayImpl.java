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
        } catch(IOException | NumberFormatException error) { throw new IllegalStateException("地区资源加载失败",error); }
    }
    private List<Area> nodes(int parent, Map<Integer,List<Row>> children) {
        return children.getOrDefault(parent,List.of()).stream().map(row->new Area(row.id(),row.name(),nodes(row.id(),children))).toList();
    }
    @Override public List<Area> chinaTree() { return tree; }
    private record Row(int id,String name) {}
}
