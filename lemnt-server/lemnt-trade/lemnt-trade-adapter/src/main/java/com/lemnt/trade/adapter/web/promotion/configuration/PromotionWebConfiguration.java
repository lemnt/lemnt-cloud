package com.lemnt.trade.adapter.web.promotion.configuration;

@org.springframework.context.annotation.Configuration
public class PromotionWebConfiguration
        implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {
    @Override
    public void extendMessageConverters(
            java.util.List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        converters.add(0, new PromotionJsonConverter());
    }
}
