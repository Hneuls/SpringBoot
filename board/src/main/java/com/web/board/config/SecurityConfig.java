package com.web.board.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/board/list", "/user/**", "/board/register").permitAll()
                .anyRequest().authenticated()
            )
    
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                logger.debug("Could not set user ");
                   response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                   response.getWriter().write("Unauthorized - Please log in");
                   response.getWriter().flush();
                })
            );

            http.addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
            
            return http.build();
    }

    @Bean
    public JwtTokenFilter jwtTokenFilter() {
        return new JwtTokenFilter(jwtTokenProvider, userDetailsService);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        // DaoAuthenticationProvider는 Spring Security의 AuthenticationProvider 구현체로, 데이터베이스에서 사용자 정보를 조회하여 인증을 처리합니다
        authenticationProvider.setUserDetailsService(userDetailsService); 
        // userDetailsService는 사용자 정보를 제공하는 서비스로, DaoAuthenticationProvider가 사용자 정보를 조회하는 데 사용됩니다.
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        // PasswordEncoder는 비밀번호를 암호화하고 데이터 베이스의 비밀번호와 비교하는 역할을 합니다.
        return authenticationProvider;
        /*
        인증이 성공하면 Authentication 객체가 반환되고, Spring Security는 이 객체를 사용하여 인증된 사용자의 정보를 관리합니다.
        인증이 실패하면, 예외가 발생하고 로그인 실패 페이지로 리다이렉트됩니다. 
        */
    }
   
}
