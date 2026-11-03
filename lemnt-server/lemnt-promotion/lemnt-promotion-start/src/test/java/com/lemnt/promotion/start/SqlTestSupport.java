package com.lemnt.promotion.start;
import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javax.sql.DataSource;
/** 每个SqlSessionFactory只装载对应服务的Mapper，不能用混库掩盖跨域SQL。 */
public final class SqlTestSupport {
    private SqlTestSupport() {}
    public static SqlSessionTemplate session(String context,DataSource source) throws Exception {
        var resources=new ArrayList<Resource>();
        for(var resource:new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml")) {
            try(var input=resource.getInputStream()) {
                if(new String(input.readAllBytes(),StandardCharsets.UTF_8).contains("namespace=\"com.lemnt."+context+".infrastructure.")) resources.add(resource);
            }
        }
        var config=new MybatisConfiguration();config.setMapUnderscoreToCamelCase(true);
        var interceptor=new MybatisPlusInterceptor();interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));config.addInterceptor(interceptor);
        var bean=new MybatisSqlSessionFactoryBean();bean.setDataSource(source);bean.setConfiguration(config);
        bean.setGlobalConfig(new GlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler()));
        bean.setMapperLocations(resources.toArray(Resource[]::new));
        var factory=bean.getObject();config.addMappers("com.lemnt."+context+".infrastructure");return new SqlSessionTemplate(factory);
    }
}
