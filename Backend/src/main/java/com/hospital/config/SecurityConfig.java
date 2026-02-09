package com.hospital.config;

import com.hospital.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final String allowedOrigins;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthenticationFilter,
      @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.allowedOrigins = allowedOrigins;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/health").permitAll()
            .requestMatchers(HttpMethod.POST, "/doctor/availability")
              .hasRole("DOCTOR")
            .requestMatchers(HttpMethod.GET, "/doctor/*/availability")
              .hasAnyRole("DOCTOR", "ADMIN", "PATIENT")
            .requestMatchers(HttpMethod.POST, "/availability")
              .hasRole("DOCTOR")
            .requestMatchers(HttpMethod.GET, "/availability/doctor/**")
              .hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
            .requestMatchers(HttpMethod.GET, "/appointments/available-slots")
              .hasAnyRole("PATIENT", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/appointments/request", "/appointments/book")
              .hasRole("PATIENT")
            .requestMatchers(HttpMethod.GET, "/appointments/patient", "/appointments/my")
              .hasRole("PATIENT")
            .requestMatchers(HttpMethod.GET, "/appointments/doctor")
              .hasRole("DOCTOR")
            .requestMatchers(HttpMethod.PUT, "/appointments/*/accept", "/appointments/*/reject")
              .hasRole("DOCTOR")
            .requestMatchers(HttpMethod.PUT, "/appointments/*/cancel", "/appointments/*/reschedule")
              .hasRole("PATIENT")
            .requestMatchers(HttpMethod.PUT, "/appointments/*/status")
              .hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/appointments")
              .hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/files/upload")
              .hasRole("DOCTOR")
            .requestMatchers(HttpMethod.GET, "/files/**")
              .hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
            .requestMatchers(HttpMethod.GET, "/audit-logs/**")
              .hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/medical-records")
              .hasAnyRole("DOCTOR", "ADMIN")
            .requestMatchers(HttpMethod.GET, "/medical-records")
              .hasAnyRole("ADMIN", "DOCTOR")
            .requestMatchers(HttpMethod.GET, "/medical-records/my")
              .hasRole("PATIENT")
            .requestMatchers(HttpMethod.GET, "/medical-records/patient/**")
              .hasAnyRole("ADMIN", "DOCTOR")
            .requestMatchers(HttpMethod.POST, "/prescriptions")
              .hasAnyRole("DOCTOR", "ADMIN")
            .requestMatchers(HttpMethod.GET, "/doctors/**")
              .hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
            .requestMatchers("/patients/**")
              .hasAnyRole("ADMIN", "DOCTOR")
            .requestMatchers("/prescriptions/**")
              .hasAnyRole("ADMIN", "DOCTOR")
            .requestMatchers("/profile/**")
              .hasRole("PATIENT")
            .requestMatchers("/admin/**")
              .hasRole("ADMIN")
            .anyRequest().hasRole("ADMIN")
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(
                (request, response, authException) -> response.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
            .accessDeniedHandler(
                (request, response, accessDeniedException) -> response.setStatus(HttpServletResponse.SC_FORBIDDEN))
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable());

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(parseAllowedOrigins());
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  private List<String> parseAllowedOrigins() {
    return Arrays.stream(allowedOrigins.split(","))
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .toList();
  }
}
