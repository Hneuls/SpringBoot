package com.web.board.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication) 
                                        throws IOException, ServletException {

        HttpSession session = request.getSession(); 
        // 현재 HTTP 요청에 대한 세션을 가져옵니다. 만약 요청에 세션이 없다면, 새로운 세션을 생성
        // 세션으로 사용자의 정보를 세션에 저장하며 이후에도 이 정보를 사용하기 위함
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        // 인증 성공 후의 Authentication 객체에서 Principal을 가져옵니다. Principal은 인증된 사용자에 대한 정보를 담고 있는 객체
        session.setAttribute("nickname", userDetails.getNickname());

        response.sendRedirect("/board/list"); // 로그인 성공 후 리다이렉트 할 경로
        
    }
}
