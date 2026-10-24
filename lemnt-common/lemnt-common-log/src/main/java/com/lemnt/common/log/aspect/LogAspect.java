package com.lemnt.common.log.aspect;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import cn.hutool.core.convert.Convert;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.lemnt.common.log.event.OperLogEvent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.common.core.utils.ExceptionUtil;
import com.lemnt.common.core.utils.ServletUtils;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.core.utils.ip.IpUtils;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessStatus;
import com.lemnt.common.log.service.AsyncLogService;
import com.lemnt.common.security.utils.SecurityUtils;

/**
 * 操作日志记录处理
 *
 * @author wangxx
 */
@Aspect
@Component
public class LogAspect {
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    /**
     * 操作标题
     */
    public static final String OPERATION_TITLE_HEADER = "X-Operation-Title";
    /**
     * 操作名称
     */
    public static final String OPERATION_NAME_HEADER = "X-Operation-Name";

    /**
     * 排除敏感属性字段
     */
    public static final String[] EXCLUDE_PROPERTIES = {"password", "oldPassword", "newPassword", "confirmPassword"};

    /**
     * 计算操作消耗时间
     */
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<Long>("Cost Time");

    /**
     * 参数最大长度限制
     */
    private static final int PARAM_MAX_LENGTH = 2000;

    @Autowired
    private AsyncLogService asyncLogService;

    /**
     * 处理请求前执行
     */
    @Before(value = "@annotation(controllerLog)")
    public void doBefore(JoinPoint joinPoint, Log controllerLog) {
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            // *========数据库日志=========*//
            OperLogEvent addCmd = new OperLogEvent();
            addCmd.setStatus(BusinessStatus.SUCCESS.getCode());
            // 请求的地址
            String ip = IpUtils.getIpAddr();
            addCmd.setOperIp(ip);
            addCmd.setOperUrl(StringUtils.substring(ServletUtils.getRequest().getRequestURI(), 0, 255));
            String username = SecurityUtils.getUsername();
            if (StringUtils.isNotBlank(username)) {
                addCmd.setOperName(username);
            }

            if (e != null) {
                addCmd.setStatus(BusinessStatus.FAIL.getCode());
                addCmd.setErrorMsg(StringUtils.substring(Convert.toStr(e.getMessage(), ExceptionUtil.getExceptionMessage(e)), 0, 2000));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            addCmd.setMethod(className + "." + methodName + "()");
            // 设置请求方式
            addCmd.setRequestMethod(ServletUtils.getRequest().getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, addCmd, jsonResult);
            // 设置消耗时间
            addCmd.setCostTime(System.currentTimeMillis() - TIME_THREADLOCAL.get());
            addCmd.setOperTime(LocalDateTime.now());
            // 保存数据库
            asyncLogService.saveSysLog(addCmd);
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("异常信息:", exp);
        } finally {
            TIME_THREADLOCAL.remove();
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param log    日志
     * @param addCmd 操作日志
     * @throws Exception
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, Log log, OperLogEvent addCmd, Object jsonResult) throws Exception {
        // 设置action动作
        addCmd.setBusinessType(log.businessType().ordinal());
        addCmd.setBusinessName(StringUtils.defaultIfBlank(ServletUtils.getHeader(Objects.requireNonNull(ServletUtils.getRequest()), OPERATION_NAME_HEADER), log.businessName()));
        // 设置标题
        addCmd.setTitle(StringUtils.defaultIfBlank(ServletUtils.getHeader(Objects.requireNonNull(ServletUtils.getRequest()), OPERATION_TITLE_HEADER), log.title()));
        // 设置操作人类别
        addCmd.setOperatorType(log.operatorType().ordinal());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, addCmd, log.excludeParamNames());
        }
        // 是否需要保存response，参数和值
        if (log.isSaveResponseData() && Objects.nonNull(jsonResult)) {
            addCmd.setJsonResult(StringUtils.substring(JsonUtils.toJsonStr(jsonResult), 0, 2000));
        }
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param addCmd 操作日志
     * @throws Exception 异常
     */
    private void setRequestValue(JoinPoint joinPoint, OperLogEvent addCmd, String[] excludeParamNames) throws Exception {
        String requestMethod = addCmd.getRequestMethod();
        Map<?, ?> paramsMap = ServletUtils.getParamMap(ServletUtils.getRequest());
        if (StringUtils.isEmpty(paramsMap) && StringUtils.equalsAny(requestMethod, HttpMethod.PUT.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name())) {
            String params = argsArrayToString(joinPoint.getArgs(), excludeParamNames);
            addCmd.setOperParam(params);
        } else {
            addCmd.setOperParam(StringUtils.substring(JsonUtils.toJsonStr(paramsMap), 0, PARAM_MAX_LENGTH));
        }
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (Objects.nonNull(o) && !isFilterObject(o)) {
                    try {
                        String jsonObj = JsonUtils.toJsonStr(o);
                        params.append(jsonObj).append(" ");
                        if (params.length() >= PARAM_MAX_LENGTH) {
                            return StringUtils.substring(params.toString(), 0, PARAM_MAX_LENGTH);
                        }
                    } catch (Exception e) {
                        log.error("请求参数拼装异常 msg:{}, 参数:{}", e.getMessage(), paramsArray, e);
                    }
                }
            }
        }
        return params.toString();
    }


    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}
