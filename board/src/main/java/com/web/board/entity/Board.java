package com.web.board.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity // 자바의 JPA에서 테이블을 의미
@Data // @Getter / @Setter / @ToString / @EqualsAndHashCode / @RequiredArgsConstructor 를 합쳐놓은 어노테이션
public class Board {

    @Id // primary key 의미
    @GeneratedValue(strategy = GenerationType.IDENTITY) // GenerationType.IDENTITY를 사용하면 Auto increment가 됨 
    private Integer id;
    
    private String title;

    private String content;

    private String filename;

    private String filepath;

    @ManyToOne // 여러개의 게시글을 하나의 사용자와 연관이 있을 수 있다.
    @JoinColumn(name = "user_id") // Board 테이블에 user_id 컬럼이 생성되고, 이 컬럼은 User 테이블의 id 컬럼을 참조함
    // 이 설정을 통해 Board 객체는 User 객체를 참조할 수 있게 됨
    // 데이터베이스의 외래 키와 기본 키를 자동으로 매핑하기 때문
    private User author;

}
