package com.lemnt.trade.adapter.web.promotion;

import com.lemnt.trade.adapter.web.promotion.res.PromotionErrorRes;
import com.lemnt.trade.domain.promotion.model.*;
import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes = PromotionAdminController.class)
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class PromotionExceptionHandler {
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<R<PromotionErrorRes>> login(NotLoginException e) {
        return response(401, "ADMIN_LOGIN_REQUIRED", false, "LOGIN");
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<R<PromotionErrorRes>> forbidden(NotPermissionException e) {
        return response(403, "PROMOTION_PERMISSION_DENIED", false, "FIX_INPUT");
    }

    @ExceptionHandler(PromotionCatalogUnavailableException.class)
    public ResponseEntity<R<PromotionErrorRes>> catalog(PromotionCatalogUnavailableException e) {
        return response(503, "CATALOG_FACTS_UNAVAILABLE", true, "REFRESH_ADMIN");
    }

    @ExceptionHandler(PromotionPersistenceException.class)
    public ResponseEntity<R<PromotionErrorRes>> persistence(PromotionPersistenceException e) {
        boolean missing = e.code() == PromotionPersistenceException.Code.PROMOTION_NOT_FOUND;
        return response(missing ? 404 : 409, e.code().name(), false, "REFRESH_ADMIN");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<R<PromotionErrorRes>> invalid(IllegalArgumentException e) {
        return response(
                400,
                e instanceof PricingException p ? p.code().name() : "INVALID_PROMOTION_REQUEST",
                false,
                "FIX_INPUT");
    }

    @ExceptionHandler({
        org.springframework.http.converter.HttpMessageNotReadableException.class,
        org.springframework.validation.BindException.class,
        org.springframework.beans.TypeMismatchException.class
    })
    public ResponseEntity<R<PromotionErrorRes>> binding(Exception e) {
        return response(400, "INVALID_PROMOTION_REQUEST", false, "FIX_INPUT");
    }

    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public ResponseEntity<R<PromotionErrorRes>> duplicate(
            org.springframework.dao.DuplicateKeyException e) {
        return response(409, "PROMOTION_CODE_CONFLICT", false, "FIX_INPUT");
    }

    @ExceptionHandler({
        org.springframework.dao.TransientDataAccessException.class,
        org.springframework.dao.DataAccessResourceFailureException.class
    })
    public ResponseEntity<R<PromotionErrorRes>> unavailable(Exception e) {
        return response(503, "PRICING_TEMPORARILY_UNAVAILABLE", true, "REFRESH_ADMIN");
    }

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
                        code, retryable, action, java.util.UUID.randomUUID().toString()));
        return ResponseEntity.status(status).body(r);
    }
}
