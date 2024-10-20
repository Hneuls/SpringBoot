package com.web.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web.board.entity.Board;
import com.web.board.entity.Likes;
import com.web.board.entity.User;

@Repository
public interface LikeRepository extends JpaRepository<Likes, Integer> {
    boolean existsByBoardAndUser(Board board, User user);
    void deleteByBoardAndUser(Board board, User user);
}
