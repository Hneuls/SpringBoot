package com.web.board.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web.board.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByLoginId(String loginId);
    boolean existsByNickname(String nickname);
    Optional<User> findByLoginId(String loginId); // Optional = User 타입의 값이 있을 수도 있고 없을 수도 있는 컨테이너
}

