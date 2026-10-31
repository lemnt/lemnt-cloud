package com.lemnt.trade.start;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

/** 测试夹具：装配真实按表组件和事务代理，不替代业务实现。 */
public final class TableTestComponents {
    private final SqlSessionTemplate session;
    private final DataSource source;
    private final Map<Class<?>, Object> components = new HashMap<>();

    public TableTestComponents(SqlSessionTemplate session, DataSource source) {
        this.session = session;
        this.source = source;
    }

    public <T> void register(Class<T> type, T value) {
        components.put(type, value);
    }

    public <T> T get(Class<T> type) {
        if (components.containsKey(type)) { return type.cast(components.get(type)); }
        if (type.getSimpleName().endsWith("Mapper")) { return session.getMapper(type); }
        try {
            var implementation = type.isInterface()
                    ? Class.forName(type.getName().replace(".domain.", ".infrastructure.") + "Impl")
                    : type;
            var constructor = implementation.getConstructors()[0];
            var args = Arrays.stream(constructor.getParameterTypes()).map(this::get).toArray();
            var target = constructor.newInstance(args);
            var proxy = new ProxyFactory(target);
            proxy.setProxyTargetClass(!type.isInterface());
            proxy.addAdvice(new TransactionInterceptor(new DataSourceTransactionManager(source),
                    new AnnotationTransactionAttributeSource()));
            var value = proxy.getProxy();
            components.put(type, value);
            return type.cast(value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("无法装配测试组件: " + type.getName(), exception);
        }
    }
}
