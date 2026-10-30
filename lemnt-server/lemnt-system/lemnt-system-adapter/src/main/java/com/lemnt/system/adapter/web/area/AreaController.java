package com.lemnt.system.adapter.web.area;
import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresLogin;
import com.lemnt.system.adapter.web.area.res.AreaTreeRes;
import com.lemnt.system.application.area.dto.AreaDTO;
import com.lemnt.system.application.area.executor.AreaTreeQueryExe;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/** 后台通用地区查询。 */
@RestController @RequestMapping("/areas") @RequiredArgsConstructor
public class AreaController {
    private final AreaTreeQueryExe query;
    /** 已登录后台读取中国省市区树。 */
    @GetMapping("/tree") @RequiresLogin
    public R<List<AreaTreeRes>> tree() { return R.ok(query.execute().stream().map(this::toRes).toList()); }
    private AreaTreeRes toRes(AreaDTO v) { return new AreaTreeRes(v.id(),v.name(),v.children().stream().map(this::toRes).toList()); }
}
