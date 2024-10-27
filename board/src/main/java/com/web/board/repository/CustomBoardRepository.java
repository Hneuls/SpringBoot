package com.web.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.web.board.entity.Board;

public interface CustomBoardRepository {
    Page<Board> searchByTitle(String searchKeyword, Pageable pageable);
    
}
