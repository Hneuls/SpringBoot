package com.web.board.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity // 자바의 JPA에서 테이블을 의미
@Data // @Getter / @Setter / @ToString / @EqualsAndHashCode / @RequiredArgsConstructor 를 합쳐놓은 어노테이션
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String loginId;

    private String nickname;

    private String password;
}
