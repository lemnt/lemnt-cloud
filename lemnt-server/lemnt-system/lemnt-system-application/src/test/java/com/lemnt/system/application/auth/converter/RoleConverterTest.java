package com.lemnt.system.application.auth.converter;

import com.lemnt.system.domain.auth.model.Role;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleConverterTest {

    @Test
    void shouldPreserveRoleMetadataWithoutTreeSettings() {
        Role role = new Role()
                .setRoleId(4L)
                .setRoleName("运营")
                .setRoleKey("operator")
                .setRoleSort(2)
                .setDataScope("2")
                .setStatus("1")
                .setFlag(true);

        var result = RoleConverter.toDTO(role);

        assertThat(result).usingRecursiveComparison().comparingOnlyFields(
                "roleId", "roleName", "roleKey", "roleSort", "dataScope", "status", "flag")
                .isEqualTo(role);
        assertThat(RoleConverter.toDTO(null)).isNull();
    }
}
