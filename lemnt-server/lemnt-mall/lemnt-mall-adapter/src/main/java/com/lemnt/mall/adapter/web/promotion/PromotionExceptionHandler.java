package com.lemnt.mall.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.exception.NotLoginException;
import com.lemnt.common.security.exception.NotPermissionException;
import com.lemnt.mall.adapter.web.promotion.res.PromotionErrorRes;
import com.lemnt.mall.domain.promotion.model.PricingException;
import com.lemnt.mall.domain.promotion.model.PromotionCatalogUnavailableException;
import com.lemnt.mall.domain.promotion.model.PromotionPersistenceException;

import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

/**
 * 营销异常处理器。
 */
@RestControllerAdvice(assignableTypes = PromotionController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PromotionExceptionHandler {
    /**
     * 登录。
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<R<PromotionErrorRes>> login(NotLoginException e) {
        return response(401, "ADMIN_LOGIN_REQUIRED", false, "LOGIN");
    }

    /**
     * 禁止访问。
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<R<PromotionErrorRes>> forbidden(NotPermissionException e) {
        return response(403, "PROMOTION_PERMISSION_DENIED", false, "FIX_INPUT");
    }

    /**
     * 目录。
     */
    @ExceptionHandler(PromotionCatalogUnavailableException.class)
    public ResponseEntity<R<PromotionErrorRes>> catalog(PromotionCatalogUnavailableException e) {
        return response(503, "CATALOG_FACTS_UNAVAILABLE", true, "REFRESH_ADMIN");
    }

    /**
     * 持久化。
     */
    @ExceptionHandler(PromotionPersistenceException.class)
    public ResponseEntity<R<PromotionErrorRes>> persistence(PromotionPersistenceException e) {
        boolean missing = e.code() == PromotionPersistenceException.Code.PROMOTION_NOT_FOUND;
        return response(missing ? 404 : 409, e.code().name(), false, "REFRESH_ADMIN");
    }

    /**
     * 参数无效。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<R<PromotionErrorRes>> invalid(IllegalArgumentException e) {
        return response(
                400,
                e instanceof PricingException p ? p.code().name() : "INVALID_PROMOTION_REQUEST",
                false,
        "FIX_INPUT");
    }

    /**
     * 第三方绑定信息。
     */
    @ExceptionHandler( {
            HttpMessageNotReadableException.class,
            BindException.class,
            TypeMismatchException.class
            })
    public ResponseEntity<R<PromotionErrorRes>> binding(Exception e) {
        return response(400, "INVALID_PROMOTION_REQUEST", false, "FIX_INPUT");
    }

    /**
     * 重复。
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<R<PromotionErrorRes>> duplicate(
            DuplicateKeyException e) {
        return response(409, "PROMOTION_CODE_CONFLICT", false, "FIX_INPUT");
    }

    /**
     * 暂不可用。
     */
    @ExceptionHandler( {
            TransientDataAccessException.class,
            DataAccessResourceFailureException.class
            })
    public ResponseEntity<R<PromotionErrorRes>> unavailable(Exception e) {
        return response(503, "PRICING_TEMPORARILY_UNAVAILABLE", true, "REFRESH_ADMIN");
    }

    /**
     * 其他。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<PromotionErrorRes>> other(Exception e) {
        return response(500, "PROMOTION_INTERNAL_ERROR", false, "REFRESH_ADMIN");
    }

    private ResponseEntity<R<PromotionErrorRes>> response(
            int status, String code, boolean retryable, String action) {
        var r = new R<PromotionErrorRes>();
        r.setCode(status);
        r.setMsg("促销请求未完成");
        r.setData(
                new PromotionErrorRes(
                code, retryable, action, UUID.randomUUID().toString()));
        return ResponseEntity.status(status).body(r);
    }
}
