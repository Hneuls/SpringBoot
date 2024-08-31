package com.web.board.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.web.board.entity.User;
import com.web.board.repository.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor // 모든 필드를 매개변수로 가지는 생성자를 자동으로 생성.
// @NoArgsConstructor // 매개변수가 없는 기본 생성자를 자동으로 생성

@Service
public class CustomUserDetailsService implements UserDetailsService{
    
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId /*사용자가 로그인 폼에 입력한 값*/) throws UsernameNotFoundException {
        User user = userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾지 못했습니다"));
            // orElseThrow 메서드는 Optional 객체의 값이 존재하지않을 때 예외를 던지게 함
            // User 객체가 존재하지 않을 때의 예외 처리를 안전하고 깔끔하게 처리할 수 있음
            return new CustomUserDetails(user);

            
    }

    
}
