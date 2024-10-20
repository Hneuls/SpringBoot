package com.web.board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    public SecurityConfig(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/board/list", "/user/**", "/board/register").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login") // 인증되지 않은 사용자가 접근이 제한된 페이지에 접근하려고 할 때, Spring Security가 사용자를 /login 페이지로 리다이렉트하도록 설정
                .loginProcessingUrl("/login") // login 엔드포인트로 들어오는 POST 요청을 스프링 시큐리티의 로그인 인증 필터가 처리하도록 합니다.
                .usernameParameter("login_id")  // 로그인 폼에서 사용하는 입력 필드의 이름을 지정합니다
                .passwordParameter("password") 
                .successHandler(customAuthenticationSuccessHandler) // 로그인 성공 시 호출될 커스텀 성공 핸들러를 지정합니다
                .failureUrl("/login?error=true") // 로그인 실패 시 리다이렉트될 URL을 지정합니다.
                .permitAll() // 로그인 페이지와 로그인 처리를 모든 사용자에게 허용합니다. 사용자가 인증되지 않은 상태에서도 로그인 페이지에 접근하고 로그인할 수 있도록 허용합니다.


            )
            .logout(logout -> logout
            .logoutUrl("/logout") // 로그아웃 요청 URL
            .logoutSuccessUrl("/board/list") // 로그아웃 후 리다이렉트할 URL
            .invalidateHttpSession(true) // 세션 무효화
            .deleteCookies("JSESSIONID") // 쿠키 삭제
            .permitAll()
            )

            .exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) -> {
                response.sendRedirect("/login?error=login_required"); // 로그인이 안된 상태에서 인증이 필요한 사이트에 접속했을 때 이 사이트로 리다이렉트 
             })
            );

        return http.build();
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
