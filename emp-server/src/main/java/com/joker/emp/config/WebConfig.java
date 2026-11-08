package com.joker.emp.config;

import com.joker.emp.security.AuthInterceptor;
import com.joker.emp.security.JwtProperties;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private static final List<String> DEV_ORIGINS = List.of(
      "http://localhost:3000",
      "http://127.0.0.1:3000",
      "http://localhost:5173",
      "http://127.0.0.1:5173");

  private final AuthInterceptor authInterceptor;
  private final RedisApiResponseCache redisApiResponseCache;
  private final JwtProperties jwtProperties;

  public WebConfig(AuthInterceptor authInterceptor, RedisApiResponseCache redisApiResponseCache,
      JwtProperties jwtProperties) {
    this.authInterceptor = authInterceptor;
    this.redisApiResponseCache = redisApiResponseCache;
    this.jwtProperties = jwtProperties;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/auth/logout",
            "/files/view/**",
            "/certificates/verify/**",
            "/error"
        );
    registry.addInterceptor(redisApiResponseCache)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/auth/logout",
            "/files/view/**",
            "/certificates/verify/**",
            "/error"
        );
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    List<String> configured = jwtProperties.getAllowedOrigins();
    List<String> origins = configured == null ? List.of() : configured.stream()
        .filter(origin -> origin != null && !origin.isBlank() && !"*".equals(origin.trim()))
        .map(String::trim)
        .toList();
    if (origins.isEmpty()) {
      origins = DEV_ORIGINS;
    }
    registry.addMapping("/**")
        .allowedOrigins(origins.toArray(String[]::new))
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("Authorization", "Content-Type", "Accept", "Range")
        .exposedHeaders("Content-Disposition", "Content-Range", "Accept-Ranges")
        .allowCredentials(true)
        .maxAge(3600);
  }
}
