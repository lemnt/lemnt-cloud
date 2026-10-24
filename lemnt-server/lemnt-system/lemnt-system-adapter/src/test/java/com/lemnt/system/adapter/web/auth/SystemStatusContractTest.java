package com.lemnt.system.adapter.web.auth;

import com.lemnt.common.core.constant.UserConstants;
import com.lemnt.common.log.enums.BusinessStatus;
import com.lemnt.common.security.enums.LoginStatusEnum;
import com.lemnt.common.security.enums.StatusEnum;
import com.lemnt.common.security.enums.UserStatusEnum;
import com.lemnt.system.adapter.web.auth.req.UserStatusUpdateReq;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;

import static org.assertj.core.api.Assertions.assertThat;

class SystemStatusContractTest {

    @Test
    void shouldUseOneForNormalAndSuccessAndTwoForDisabledAndFailure() {
        assertThat(UserConstants.NORMAL).isEqualTo("1");
        assertThat(UserConstants.USER_DISABLE).isEqualTo("2");
        assertThat(UserConstants.ROLE_NORMAL).isEqualTo("1");
        assertThat(UserConstants.ROLE_DISABLE).isEqualTo("2");
        assertThat(UserConstants.DEPT_NORMAL).isEqualTo("1");
        assertThat(UserConstants.DEPT_DISABLE).isEqualTo("2");
        assertThat(UserConstants.DICT_NORMAL).isEqualTo("1");
        assertThat(StatusEnum.NORMAL.getState()).isEqualTo(1);
        assertThat(StatusEnum.DISABLE.getState()).isEqualTo(2);
        assertThat(UserStatusEnum.OK.getCode()).isEqualTo("1");
        assertThat(UserStatusEnum.DISABLE.getCode()).isEqualTo("2");
        assertThat(LoginStatusEnum.SUCCESS.getCode()).isEqualTo("1");
        assertThat(LoginStatusEnum.FAIL.getCode()).isEqualTo("2");
        assertThat(BusinessStatus.SUCCESS.getCode()).isEqualTo(1);
        assertThat(BusinessStatus.FAIL.getCode()).isEqualTo(2);
    }

    @Test
    void shouldRejectLegacyAndInvalidAccountStatusValues() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (String status : new String[]{"1", "2"}) {
                assertThat(validator.validate(new UserStatusUpdateReq(4L, status))).isEmpty();
            }
            for (String status : new String[]{"0", "3", "12", ""}) {
                assertThat(validator.validate(new UserStatusUpdateReq(4L, status)))
                        .anySatisfy(violation -> assertThat(violation.getPropertyPath().toString()).isEqualTo("status"));
            }
        }
    }
}
