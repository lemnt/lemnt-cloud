package com.lemnt.common.security.handler;

import com.lemnt.common.security.exception.NotPermissionException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnProblemDetailsForV1PermissionFailure() {
        Object result = handler.handleNotPermissionException(
                new NotPermissionException("example:record:list"), request("/api/v1/campaigns"));

        ResponseEntity<?> response = assertProblemResponse(result, 403, "FORBIDDEN");
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(((ProblemDetail) response.getBody()).getInstance()).hasToString("/api/v1/campaigns");
    }

    @Test
    void shouldReturnValidationErrorsForV1BindingFailure() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "campaign");
        bindingResult.addError(new FieldError("campaign", "campaignName", "计划名称不能为空"));

        Object result = handler.handleBindException(new BindException(bindingResult),
                request("/api/v1/campaigns"));

        ResponseEntity<?> response = assertProblemResponse(result, 422, "VALIDATION_ERROR");
        ProblemDetail body = (ProblemDetail) response.getBody();
        assertThat(body.getProperties()).containsKey("errors");
        assertThat((List<?>) body.getProperties().get("errors")).hasSize(1);
    }

    @Test
    void shouldReturnProblemDetailsWithoutLegacyEnvelope() {
        Object result = handler.handleNotPermissionException(
                new NotPermissionException("example:record:list"), request("/campaign/list"));

        assertProblemResponse(result, 403, "FORBIDDEN");
    }

    private ResponseEntity<?> assertProblemResponse(Object result, int status, String code) {
        assertThat(result).isInstanceOf(ResponseEntity.class);
        ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertThat(response.getStatusCode().value()).isEqualTo(status);
        assertThat(response.getBody()).isInstanceOf(ProblemDetail.class);
        ProblemDetail body = (ProblemDetail) response.getBody();
        assertThat(body.getStatus()).isEqualTo(status);
        assertThat(body.getProperties()).containsEntry("code", code);
        return response;
    }

    private MockHttpServletRequest request(String path) {
        return new MockHttpServletRequest("GET", path);
    }
}
