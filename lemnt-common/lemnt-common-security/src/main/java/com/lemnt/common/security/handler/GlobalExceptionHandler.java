package com.lemnt.common.security.handler;

import com.lemnt.common.core.constant.HttpStatus;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.exception.NotPermissionException;
import com.lemnt.common.security.exception.NotRoleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 *
 * @author wangxx
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 权限码异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Object handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        log.warn("请求地址'{}',权限码校验失败'{}'", request.getRequestURI(), e.getMessage());
        return respond(request, HttpStatus.FORBIDDEN, "FORBIDDEN", "没有访问权限", "没有访问权限，请联系管理员授权");
    }

    /**
     * 角色权限异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Object handleNotRoleException(NotRoleException e, HttpServletRequest request) {
        log.warn("请求地址'{}',角色权限校验失败'{}'", request.getRequestURI(), e.getMessage());
        return respond(request, HttpStatus.FORBIDDEN, "FORBIDDEN", "没有访问权限", "没有访问权限，请联系管理员授权");
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                       HttpServletRequest request) {
        log.warn("请求地址'{}',不支持'{}'请求", request.getRequestURI(), e.getMethod());
        return respond(request, org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED.value(),
                "METHOD_NOT_ALLOWED", "请求方法不支持", e.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public Object handleServiceException(ServiceException e, HttpServletRequest request) {
        log.warn("业务异常: {}", e.getMessage(), e);
        Integer code = e.getCode();
        int status = isErrorStatus(code) ? code : org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY.value();
        return problem(request, status, "BUSINESS_RULE_VIOLATION", "业务规则校验失败", e.getMessage(), null);
    }

    /**
     * 请求路径中缺少必需的路径变量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public Object handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
        log.error("请求路径中缺少必需的路径变量'{}',发生系统异常.", request.getRequestURI());
        return respond(request, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR", "服务器内部错误", "系统异常，请联系管理员");
    }

    /**
     * 请求参数缺失。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleMissingServletRequestParameterException(MissingServletRequestParameterException e,
                                                                 HttpServletRequest request) {
        String message = String.format("请求参数[%s]不能为空", e.getParameterName());
        return respond(request, org.springframework.http.HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST", "请求参数错误", message);
    }

    /**
     * 显式 HTTP 状态异常。
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Object handleResponseStatusException(ResponseStatusException e, HttpServletRequest request) {
        int status = e.getStatusCode().value();
        String detail = Objects.requireNonNullElse(e.getReason(), "请求失败");
        return respond(request, status, errorCode(status), title(status), detail);
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常 uri={}", request.getRequestURI(), e);
        return respond(request, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR", "服务器内部错误", "系统繁忙，请稍后重试");
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 uri={}", request.getRequestURI(), e);
        return respond(request, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR", "服务器内部错误", "系统异常，请联系管理员");
    }

    /**
     * 参数绑定异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    public Object handleBindException(BindException e, HttpServletRequest request) {
        FieldError error = e.getFieldError();
        String message = Objects.nonNull(error) ? error.getDefaultMessage() : "参数绑定异常";
        log.error("参数绑定异常: field={}, rejectedValue={}, message={}",
                Objects.nonNull(error) ? error.getField() : null,
                Objects.nonNull(error) ? error.getRejectedValue() : null,
                message);
        return validationProblem(request, message, e.getFieldErrors());
    }

    /**
     * 参数校验异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                        HttpServletRequest request) {
        FieldError error = e.getBindingResult().getFieldError();
        String message = Objects.nonNull(error) ? error.getDefaultMessage() : "参数校验失败";
        log.error("参数校验失败: field={}, rejectedValue={}, message={}",
                Objects.nonNull(error) ? error.getField() : null,
                Objects.nonNull(error) ? error.getRejectedValue() : null,
                message);
        return validationProblem(request, message, e.getBindingResult().getFieldErrors());
    }

    private Object validationProblem(HttpServletRequest request, String message, List<FieldError> fieldErrors) {
        List<ValidationError> errors = fieldErrors.stream()
                .map(error -> new ValidationError("/" + error.getField(), error.getDefaultMessage()))
                .toList();
        return problem(request, org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "VALIDATION_ERROR", "请求参数校验失败", message, errors);
    }

    private Object respond(HttpServletRequest request, int status, String code, String title, String detail) {
        return problem(request, status, code, title, detail, null);
    }

    private ResponseEntity<ProblemDetail> problem(HttpServletRequest request, int status, String code,
                                                   String title, String detail, List<ValidationError> errors) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(
                org.springframework.http.HttpStatusCode.valueOf(status), detail);
        body.setType(URI.create("urn:lemnt:problem:" + code.toLowerCase().replace('_', '-')));
        body.setTitle(title);
        body.setInstance(URI.create(request.getRequestURI()));
        body.setProperty("code", code);
        String traceId = MDC.get(SecurityConstants.TRACE_ID);
        if (traceId != null) {
            body.setProperty("traceId", traceId);
        }
        if (errors != null && !errors.isEmpty()) {
            body.setProperty("errors", errors);
        }
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    private boolean isErrorStatus(Integer status) {
        return status != null && status >= 400 && status <= 599;
    }

    private String errorCode(int status) {
        return switch (status) {
            case 400 -> "BAD_REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 409 -> "CONFLICT";
            case 412 -> "PRECONDITION_FAILED";
            case 422 -> "VALIDATION_ERROR";
            case 429 -> "RATE_LIMIT_EXCEEDED";
            case 503 -> "SERVICE_UNAVAILABLE";
            default -> "INTERNAL_ERROR";
        };
    }

    private String title(int status) {
        org.springframework.http.HttpStatus httpStatus = org.springframework.http.HttpStatus.resolve(status);
        return httpStatus == null ? "请求失败" : httpStatus.getReasonPhrase();
    }

    /** 字段校验问题的稳定扩展结构。 */
    private record ValidationError(String pointer, String message) {
    }
}
