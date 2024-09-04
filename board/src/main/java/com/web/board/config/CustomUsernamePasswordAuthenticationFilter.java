package com.web.board.config;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter { // 이 클래스는 주로 인증을 시도하는 것이 역할
/*CustomUsernamePasswordAuthenticationFilter의 주요 역할은:

로그인 요청에서 사용자 입력을 추출: HttpServletRequest 객체에서 사용자가 입력한 login_id와 password를 추출합니다. 이 입력값들은 로그인 폼에서 사용자가 입력한 값

인증 토큰 생성: 추출한 login_id와 password를 기반으로 UsernamePasswordAuthenticationToken 객체를 생성합니다. 이 토큰은 Spring Security의 인증 관리자에게 전달되어 실제 인증이 수행

인증 시도: 생성된 UsernamePasswordAuthenticationToken을 사용하여 실제 인증을 시도합니다. 인증이 성공하면 사용자는 로그인된 상태로 유지

 */
// .loginProcessingUrl("/login") 이라는 SecurityConfig 설정때문에 이 요청에 대해서만 HttpServletRequest가 처리함

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String loginId = request.getParameter("login_id");
        String password = request.getParameter("password");
        // request.getParameter("login_id")는 HTML 파일이 아니라, form 데이터를 통해 서버로 전송된 요청 데이터에서 "login_id"라는 이름의 값을 찾음

        if (loginId == null || password == null) {
            throw new UsernameNotFoundException("Login ID or password not provided");
        }

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(loginId, password);
        // 인증 토큰을 만들어서 사용자가 입력한 자격 증명이 유효한지 확인
        setDetails(request, authRequest);
        // authRequest에 HttpServletRequest에서 추가 정보를 사용자의 IP 주소나 세션 ID 같은 정보를 authRequest에 추가 
        // 이 과정은 인증 프로세스에서 더 많은 컨텍스트 정보를 제공하여 보안을 강화하거나 요청을 더 잘 추적할 수 있게 도와줌

        return this.getAuthenticationManager().authenticate(authRequest);
        // 1. getAuthenticationManager는 AuthenticationManager 객체를 반환  
        // 2. authenticate 메서드는 AuthenticationManager에게 실제 인증 작업을 하도록 요청하고, 매개변수(객체)를 기반으로 자격이 유효한지 확인
        // 3. 실패하면 authenticationException 예외 반환
    }   // 4. 인증에 성공하면 인증된 사용자 정보가 포함된 Authentication 객체를 반환하며, 이 객체는 이후의 SecutiryContext에서 사용







}
