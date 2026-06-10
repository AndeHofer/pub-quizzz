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
import org.springframework.security.web.util.matcher.RequestMatcher;
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

/**
 * Central Spring Security configuration for the application.
 *
 * <p>Usage in runtime:
 *
 * <ul>
 *   <li>Loaded as a Spring {@code @Configuration} class during application startup.</li>
 *   <li>Publishes the main {@link SecurityFilterChain} bean that defines authentication, authorization, CSRF,
 *       exception handling, and custom filter behavior.
 *   <li>Registers {@link PasswordEncoder} bean ({@link BCryptPasswordEncoder}) for password hashing/verification.
 *   <li>Enables method-level authorization checks via {@code @EnableMethodSecurity}.</li>
 * </ul>
 */
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
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(new LoggingAuthenticationEntryPoint(), apiRequestMatcher())
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

    private RequestMatcher apiRequestMatcher() {
        return request -> {
            String uri = request.getRequestURI();
            if (uri != null && uri.startsWith("/api/")) {
                return true;
            }

            String accept = request.getHeader("Accept");
            if (accept != null && accept.toLowerCase().contains("application/json")) {
                return true;
            }

            String requestedWith = request.getHeader("X-Requested-With");
            return "XMLHttpRequest".equalsIgnoreCase(requestedWith);
        };
    }

    /**
     * Internal filter that materializes the CSRF token attribute so {@link CookieCsrfTokenRepository} can write the
     * client-readable `XSRF-TOKEN` cookie.
     */
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

    /**
     * Internal filter that applies no-store cache headers to `/login` responses.
     */
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

    /**
     * Internal filter that redirects authenticated users from `GET /login` to `/`.
     */
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
