package com.lemnt.common.adapter.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RequestBindingAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RequestBindingAutoConfiguration.class))
            .withUserConfiguration(TestMvcConfiguration.class);

    @Test
    void shouldRegisterAutoConfigurationForDiscovery() {
        assertThat(ImportCandidates.load(AutoConfiguration.class, getClass().getClassLoader()))
                .contains(RequestBindingAutoConfiguration.class.getName());
    }

    @Test
    void shouldNotRegisterAdviceOutsideServletApplication() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(RequestBindingAutoConfiguration.class))
                .run(context -> assertThat(context).doesNotHaveBean(GlobalRequestBindingAdvice.class));
    }

    @Test
    void shouldIgnoreIdentityHeadersDuringRecordConstruction() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(GlobalRequestBindingAdvice.class);
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            mvc.perform(get("/binding/record")
                            .header("username", "admin")
                            .header("userid", "99"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("null:null"));
        });
    }

    @Test
    void shouldPreserveQueryAndPathValues() {
        contextRunner.run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            mvc.perform(get("/binding/record/42")
                            .header("username", "admin")
                            .header("userid", "99")
                            .param("userName", "ry"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("ry:42"));
        });
    }

    @Test
    void shouldIgnoreHeadersDuringBeanPropertyBindingAndPreserveFormValues() {
        contextRunner.run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            mvc.perform(post("/binding/bean").header("userName", "admin"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("null"));
            mvc.perform(post("/binding/bean")
                            .header("userName", "admin")
                            .param("userName", "ry"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("ry"));
        });
    }

    @Test
    void shouldPreserveExplicitHeaderAndRawRequestAccess() {
        contextRunner.run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            mvc.perform(get("/binding/header").header("username", "admin"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("admin:admin"));
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    static class TestMvcConfiguration {

        @Bean
        BindingController bindingController() {
            return new BindingController();
        }
    }

    @RestController
    static class BindingController {

        @GetMapping({"/binding/record", "/binding/record/{userId}"})
        public String record(RecordReq req) {
            return req.userName() + ":" + req.userId();
        }

        @PostMapping("/binding/bean")
        public String bean(BeanReq req) {
            return String.valueOf(req.getUserName());
        }

        @GetMapping("/binding/header")
        public String header(@RequestHeader("username") String username,
                jakarta.servlet.http.HttpServletRequest request) {
            return username + ":" + request.getHeader("username");
        }
    }

    public record RecordReq(String userName, Long userId) {
    }

    public static class BeanReq {

        private String userName;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }
}
