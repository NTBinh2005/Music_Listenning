package com.project.music_listenning.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.project.music_listenning.security.JwtAuthFilter;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // Cho phép dùng @PreAuthorize trên controller/service
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Tắt CSRF vì dùng JWT (stateless), không cần session-based protection
                .csrf(AbstractHttpConfigurer::disable)

                // 2. CORS — cho phép React dev server gọi API
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Stateless — không tạo session, mỗi request tự xác thực qua token
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Phân quyền endpoints
                .authorizeHttpRequests(auth -> auth
                        // Public — không cần đăng nhập
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/songs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/artists/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/albums/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/search/**").permitAll()

                        // Like — yêu cầu đăng nhập
                        .requestMatchers(HttpMethod.POST,   "/api/songs/*/like").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/songs/liked").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/songs/*/liked").authenticated()
                        // follow/unfollow nghệ sĩ — yêu cầu đăng nhập
                        .requestMatchers(HttpMethod.POST, "/api/artists/{id}/follow").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/artists/{id}/follow-status").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/following").authenticated()
                        // Thêm vào authorizeHttpRequests, trước anyRequest().authenticated()
                        .requestMatchers("/api/users/me").authenticated()
                        // Subscription — yêu cầu đăng nhập
                        .requestMatchers("/api/subscriptions/me").authenticated()

                        // Chỉ ADMIN mới tạo/xóa/sửa nội dung
                        .requestMatchers(HttpMethod.POST, "/api/songs/**", "/api/albums/**", "/api/artists/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/songs/**", "/api/albums/**", "/api/artists/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/songs/**", "/api/albums/**", "/api/artists/**")
                        .hasRole("ADMIN")

                        // đã đăng nhập mới được gọi
                        .requestMatchers("/api/upload/**").authenticated()
                        // Playlist — yêu cầu đăng nhập
                        .requestMatchers("/api/playlists/**").authenticated()
                        // History — yêu cầu đăng nhập
                        .requestMatchers("/api/history/**").authenticated()


                        // Còn lại yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )

                // 5. Đưa JwtAuthFilter vào trước filter xác thực username/password mặc định
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // 6. Provider xác thực
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}