package com.lemnt.order.start.support;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.apache.dubbo.config.annotation.DubboReference;
import org.h2.jdbcx.JdbcDataSource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/** 真实四套DDL、Mapper和事务；只替换RPC/MQ传输，调用线程隔离模拟远端独立事务。 */
public final class CommerceTestFixture implements AutoCloseable {
    private record Store(JdbcDataSource source, Connection schema, SqlSessionTemplate session) {}

    private final Map<String, Store> stores = new HashMap<>();
    private final Map<Class<?>, Object> beans = new HashMap<>();
    private final Map<Class<?>, Object> remotes = new HashMap<>();
    private final ExecutorService calls = Executors.newCachedThreadPool();

    @FunctionalInterface
    public interface RpcInterceptor {
        Object invoke(Class<?> type, Method method, Object[] args, Callable<Object> call)
                throws Exception;
    }

    private BiConsumer<Class<?>, Method> after = (type, method) -> {};

    public void after(BiConsumer<Class<?>, Method> hook) {
        after = hook;
    }

    public Object invoke(String type, String method, Object... args) {
        try {
            Object target = get(Class.forName(type));
            var candidate =
                    Arrays.stream(target.getClass().getMethods())
                            .filter(
                                    m ->
                                            m.getName().equals(method)
                                                    && m.getParameterCount() == args.length)
                            .findFirst()
                            .orElseThrow();
            return candidate.invoke(target, args);
        } catch (Exception e) {
            throw failure(e);
        }
    }

    private RpcInterceptor interceptor = (type, method, args, call) -> call.call();

    public CommerceTestFixture() throws Exception {
        for (String context : new String[] {"product", "promotion", "order", "pay"}) {
            var source = new JdbcDataSource();
            source.setURL(
                    "jdbc:h2:mem:"
                            + context
                            + "_"
                            + UUID.randomUUID()
                            + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000;INIT=SET TIME ZONE 'UTC'");
            var schema = source.getConnection();
            ScriptUtils.executeSqlScript(
                    schema, new ClassPathResource(context + "-sql/lemnt_" + context + ".sql"));
            var config = new MybatisConfiguration();
            config.setMapUnderscoreToCamelCase(true);
            var pagination = new MybatisPlusInterceptor();
            pagination.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
            config.addInterceptor(pagination);
            var xml = new ArrayList<Resource>();
            for (var r :
                    new PathMatchingResourcePatternResolver()
                            .getResources("classpath*:mapper/*.xml"))
                try (var input = r.getInputStream()) {
                    if (new String(input.readAllBytes(), StandardCharsets.UTF_8)
                            .contains("com.lemnt." + context + ".infrastructure.")) xml.add(r);
                }
            var factory = new MybatisSqlSessionFactoryBean();
            factory.setDataSource(source);
            factory.setConfiguration(config);
            factory.setMapperLocations(xml.toArray(Resource[]::new));
            factory.setGlobalConfig(
                    new GlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler()));
            var result = factory.getObject();
            config.addMappers("com.lemnt." + context + ".infrastructure");
            stores.put(context, new Store(source, schema, new SqlSessionTemplate(result)));
        }
    }

    public JdbcDataSource source(String context) {
        return stores.get(context).source();
    }

    public Connection schema(String context) {
        return stores.get(context).schema();
    }

    public JdbcTemplate jdbc(String context) {
        return new JdbcTemplate(source(context));
    }

    public void intercept(RpcInterceptor value) {
        interceptor = value;
    }

    public synchronized <T> void register(Class<T> type, T value) {
        beans.put(type, value);
    }

    public synchronized <T> void remote(Class<T> type, T value) {
        remotes.put(type, value);
    }

    /** 每个远端调用另起线程，不能借调用方绑定的SqlSession加入跨库事务。 */
    public <T> T call(Callable<T> action) {
        try {
            return calls.submit(action).get(20, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw failure(e.getCause());
        } catch (Exception e) {
            throw new IllegalStateException("测试传输未完成", e);
        }
    }

    private RuntimeException failure(Throwable e) {
        if (e instanceof InvocationTargetException i) return failure(i.getCause());
        if (e instanceof RuntimeException r) return r;
        return new IllegalStateException(e);
    }

    @SuppressWarnings("unchecked")
    private <T> T rpc(Class<T> type) {
        return (T)
                Proxy.newProxyInstance(
                        type.getClassLoader(),
                        new Class<?>[] {type},
                        (proxy, method, args) -> {
                            if (method.getDeclaringClass() == Object.class)
                                return method.getName().equals("toString")
                                        ? "测试RPC:" + type.getSimpleName()
                                        : System.identityHashCode(proxy);
                            return call(
                                    () ->
                                            interceptor.invoke(
                                                    type,
                                                    method,
                                                    args,
                                                    () -> {
                                                        Object provider;
                                                        synchronized (this) {
                                                            provider = remotes.get(type);
                                                        }
                                                        if (provider == null)
                                                            provider =
                                                                    get(
                                                                            Class.forName(
                                                                                    type.getName()
                                                                                                    .replace(
                                                                                                            ".client.",
                                                                                                            ".application.dubbo.")
                                                                                            + "Impl"));
                                                        return method.invoke(provider, args);
                                                    }));
                        });
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T get(Class<T> type) {
        if (beans.containsKey(type)) return type.cast(beans.get(type));
        String context = context(type);
        var store = stores.get(context);
        if (type.getSimpleName().endsWith("Mapper")) return store.session().getMapper(type);
        try {
            if (type.isInterface() && type.getPackageName().contains(".client")) {
                Object provided = remotes.get(type);
                if (provided != null) return type.cast(provided);
                return type.cast(
                        get(
                                Class.forName(
                                        type.getName().replace(".client.", ".application.dubbo.")
                                                + "Impl")));
            }
            Class<?> implementation = type;
            if (type.isInterface()) {
                String impl = type.getName().replace(".domain.", ".infrastructure.") + "Impl";
                if (type.getSimpleName().equals("OrderBenefitsGateway"))
                    impl = "com.lemnt.order.application.order.service.OrderBenefitsService";
                if (type.getSimpleName().equals("OrderRollbackGateway"))
                    impl = "com.lemnt.order.application.order.service.OrderRollbackPublisher";
                implementation = Class.forName(impl);
            }
            var constructor =
                    Arrays.stream(implementation.getConstructors())
                            .max(Comparator.comparingInt(Constructor::getParameterCount))
                            .orElseThrow();
            Object[] args = new Object[constructor.getParameterCount()];
            for (int i = 0; i < args.length; i++) args[i] = get(constructor.getParameterTypes()[i]);
            Object target = constructor.newInstance(args);
            for (var field : implementation.getDeclaredFields())
                if (field.isAnnotationPresent(DubboReference.class)) {
                    field.setAccessible(true);
                    field.set(target, rpc(field.getType()));
                }
            var factory = new ProxyFactory(target);
            factory.setProxyTargetClass(!type.isInterface());
            factory.addAdvice(
                    new TransactionInterceptor(
                            new DataSourceTransactionManager(store.source()),
                            new AnnotationTransactionAttributeSource()));
            factory.addAdvice(
                    (AfterReturningAdvice)
                            (result, method, arguments, object) -> after.accept(type, method));
            Object value = factory.getProxy();
            beans.put(type, value);
            return type.cast(value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("组合夹具无法装配:" + type.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T bean(String name) {
        try {
            return (T) get(Class.forName(name));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public String context(Class<?> type) {
        String name = type.getName();
        if (!name.startsWith("com.lemnt.")) return "order";
        String rest = name.substring("com.lemnt.".length());
        return rest.substring(0, rest.indexOf('.'));
    }

    public void close() throws Exception {
        calls.shutdownNow();
        for (var store : stores.values()) store.schema().close();
    }
}
