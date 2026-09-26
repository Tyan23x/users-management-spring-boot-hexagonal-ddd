package com.jcaa.usersmanagement.infrastructure.security;

import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

  private static final String AUTH_LOGIN_PATH = "/api/auth/login";
  private static final String HEALTH_PATH = "/api/health";
  private static final String ROOT_PATH = "/";
  private static final String USERS_PATH = "/api/users";
  private static final String USERS_DETAIL_PATH = "/api/users/**";
  private static final String[] OPEN_API_PATHS = {
    "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"
  };

  private static final String ROLE_ADMIN = "ADMIN";
  private static final String ROLE_REVIEWER = "REVIEWER";

  private static final String ALL_PATHS = "/**";
  private static final String ALL_ORIGINS = "*";

  @Bean
  public SecurityFilterChain securityFilterChain(
      final HttpSecurity http,
      final JwtAuthenticationFilter jwtAuthenticationFilter,
      final RestAuthenticationEntryPoint authenticationEntryPoint,
      final RestAccessDeniedHandler accessDeniedHandler)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(formLogin -> formLogin.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            handling ->
                handling
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .authorizeHttpRequests(
            authorization ->
                authorization
                    .requestMatchers(AUTH_LOGIN_PATH)
                    .permitAll()
                    .requestMatchers(HEALTH_PATH)
                    .permitAll()
                    .requestMatchers(ROOT_PATH)
                    .permitAll()
                    .requestMatchers(OPEN_API_PATHS)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, USERS_PATH)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, USERS_PATH, USERS_DETAIL_PATH)
                    .hasAnyRole(ROLE_ADMIN, ROLE_REVIEWER)
                    .requestMatchers(HttpMethod.PUT, USERS_DETAIL_PATH)
                    .hasRole(ROLE_ADMIN)
                    .requestMatchers(HttpMethod.DELETE, USERS_DETAIL_PATH)
                    .hasRole(ROLE_ADMIN)
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of(ALL_ORIGINS));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
    configuration.setAllowedHeaders(List.of(ALL_ORIGINS));
    configuration.setAllowCredentials(true);
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration(ALL_PATHS, configuration);
    return source;
  }
}
