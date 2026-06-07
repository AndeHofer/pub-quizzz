package com.ande.pubquizzz.security;

import com.ande.pubquizzz.database.entities.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();

        http.csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(defaultCsrfTokenRequestHandler()))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/favicon.ico", "/robots.txt").permitAll()
                        .requestMatchers("/admin/**", "/h2-console/**").hasRole(Role.ADMIN.name())
                        .anyRequest().authenticated()
                ).formLogin(Customizer.withDefaults())
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(new LoggingAccessDeniedHandler()))
                .addFilterBefore(new AuthenticatedLoginRedirectFilter(), DefaultLoginPageGeneratingFilter.class)
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .addFilterAfter(new LoginNoStoreFilter(), CsrfFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private CsrfTokenRequestHandler defaultCsrfTokenRequestHandler() {
        return new CsrfTokenRequestAttributeHandler();
    }

    private static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }

    private static final class LoginNoStoreFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            filterChain.doFilter(request, response);

            if (!"/login".equals(request.getRequestURI())) {
                return;
            }
            response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        }
    }

    private static final class AuthenticatedLoginRedirectFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            if (isAuthenticatedLoginGet(request)) {
                response.sendRedirect("/");
                return;
            }

            filterChain.doFilter(request, response);
        }

        private boolean isAuthenticatedLoginGet(HttpServletRequest request) {
            if (!"GET".equalsIgnoreCase(request.getMethod()) || !"/login".equals(request.getRequestURI())) {
                return false;
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null
                    && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken);
        }
    }
}