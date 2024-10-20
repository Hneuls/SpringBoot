package com.web.board.config;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.web.board.entity.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomUserDetails implements UserDetails {
    // 스프링 시큐리티의 UserDetails 인터페이스를 구현하여 인증 및 권한 부여 과정을 지원하는 클래스
    private User user;
    private String loginId;
    private String nickname;
    private String password;


    public CustomUserDetails(User user) {
        this.loginId = user.getLoginId();
        this.nickname = user.getNickname();
        this.password = user.getPassword();
        this.user = user;
    }

    @Override // GetUsername과 패스워드는 구현을 해야하는 구현체기때문에 getter와 setter가 따로 만들어주지않음
    public String getUsername() {
        return loginId;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자 권한을 부여하는 메서드
        // getAuthorities() 메서드에서 사용자의 권한을 반환함으로써
        // 스프링 시큐리티가 사용자 권한을 기반으로 리소스 접근을 제어할 수 있음
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
