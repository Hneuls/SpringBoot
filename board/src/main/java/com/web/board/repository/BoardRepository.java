package com.web.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web.board.entity.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer>, CustomBoardRepository {   // Board클래스와 primary키로 지정한 컬럼의 타입을 적음
}
