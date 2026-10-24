package com.lemnt.common.mybatis.handler;

import com.lemnt.common.security.context.SecurityContextHolder;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 自动填充
 *
 * @author wangxx
 * @version 1.0.0
 * @date 22/7/2025 下午 2:58
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        String username = Optional.ofNullable(SecurityContextHolder.getUserName()).orElse("system");
        this.strictInsertFill(metaObject, "createBy", String.class, username);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateBy", String.class, username);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String username = Optional.ofNullable(SecurityContextHolder.getUserName()).orElse("system");
        this.strictUpdateFill(metaObject, "updateBy", String.class, username);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
