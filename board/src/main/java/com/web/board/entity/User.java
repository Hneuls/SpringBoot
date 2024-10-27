package com.web.board.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity // 자바의 JPA에서 테이블을 의미
@Data // @Getter / @Setter / @ToString / @EqualsAndHashCode / @RequiredArgsConstructor 를 합쳐놓은 어노테이션
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id  // Primary Key 지정하는 명령어
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto Increase
    private Integer id;
    
    private String loginId;

    private String nickname;

    private String password;
}
