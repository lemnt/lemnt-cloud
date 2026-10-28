package com.lemnt.mall.infrastructure.config;

import com.lemnt.mall.infrastructure.product.mapper.ProductSkuMapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 使用既有配置的扫描路径验证 XML 与 MyBatis Plus 默认方法可共同加载。 */
class MapperXmlLoadingTest {
    @Test
    void configuredRootLocationLoadsEveryCustomStatementAndInheritedCrud() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:mapper_xml;MODE=MySQL");
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        factory.setConfiguration(new MybatisConfiguration());
        // 与项目既有 lemnt-common.yaml 保持一致，不依赖专用于测试的嵌套目录。
        factory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:/mapper/**.xml"));
        var configuration = factory.getObject().getConfiguration();
        configuration.addMappers("com.lemnt.mall.infrastructure");
        for (var mapper : configuration.getMapperRegistry().getMappers()) {
            for (var method : mapper.getDeclaredMethods()) {
                assertTrue(configuration.hasStatement(mapper.getName() + "." + method.getName()),
                        () -> "未加载 Mapper XML: " + mapper.getName() + "." + method.getName());
            }
        }
        assertTrue(configuration.hasStatement(ProductSkuMapper.class.getName() + ".selectById"));
        assertTrue(configuration.hasStatement(ProductSkuMapper.class.getName() + ".reserveStock"));
        var productResult = configuration.getMappedStatement(ProductSkuMapper.class.getName() + ".lockById")
                .getResultMaps().get(0);
        assertFalse(productResult.getAutoMapping());
        assertTrue(productResult.getPropertyResultMappings().stream()
                .anyMatch(mapping -> "productCode".equals(mapping.getProperty())
                        && "product_code".equals(mapping.getColumn())));
        var lock = configuration.getMappedStatement(ProductSkuMapper.class.getName() + ".lockById");
        assertFalse(lock.isUseCache());
        assertTrue(lock.isFlushCacheRequired());
    }
}
