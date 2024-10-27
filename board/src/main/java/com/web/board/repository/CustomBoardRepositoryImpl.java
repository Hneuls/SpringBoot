package com.web.board.repository;

import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

import com.web.board.entity.Board;


public class CustomBoardRepositoryImpl implements CustomBoardRepository {
    private final ElasticsearchOperations elasticsearchOperations;

    public CustomBoardRepositoryImpl(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public Page<Board> searchByTitle(String searchKeyword, Pageable pageable) {
        Criteria criteria = Criteria.where("title").matches(searchKeyword);
        CriteriaQuery query = new CriteriaQuery(criteria).setPageable(pageable);

        SearchHits<Board> searchHits = elasticsearchOperations.search(query, Board.class);

        return new PageImpl<>(
            searchHits.getSearchHits().stream()
            .map(hit -> hit.getContent())
            .collect(Collectors.toList()),
            pageable,
            searchHits.getTotalHits()
        );
    }
}