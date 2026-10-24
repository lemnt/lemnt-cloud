package com.lemnt.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.lemnt.common.mybatis.handler.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Mybatis 配置
 *
 * @author wangxx
 * @version 1.0.0
 * @date 22/7/2025 下午 3:14
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(Optional<DataPermissionInterceptor> dpiOptional) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 可选数据权限插件
        dpiOptional.ifPresent(interceptor::addInnerInterceptor);

        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 分页插件，限制每页最大数量防止内存撑爆
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 每页最多 1000L 条
        paginationInterceptor.setMaxLimit(1000L);
        interceptor.addInnerInterceptor(paginationInterceptor);

        // 防止全表更新/删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }
}
