package com.ande.pubquizzz.config;

import com.ande.pubquizzz.filter.NoiseFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registers the NoiseFilter.
 *
 * @since 26.6.6
 */
@Configuration
public class FilterConfig {

    /**
     * Register the NoiseFilter before Spring Security filters to ensure it can handle noise probes without triggering authentication redirects.
     */
    @Bean
    public FilterRegistrationBean<NoiseFilter> antiNoiseFilterRegistration() {
        FilterRegistrationBean<NoiseFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new NoiseFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
