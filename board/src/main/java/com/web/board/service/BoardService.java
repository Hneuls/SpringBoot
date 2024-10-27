package com.web.board.service;

import java.io.File;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.web.board.entity.Board;
import com.web.board.entity.Likes;
import com.web.board.entity.User;
import com.web.board.repository.BoardRepository;
import com.web.board.repository.LikeRepository;
import com.web.board.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class BoardService {
    
    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LikeRepository likeRepository;

    // 글 작성 처리
    public int write(Board board, MultipartFile file, User currentUser) throws Exception {
        
        if(file.isEmpty() || board.getTitle().isEmpty()) {
            throw new IllegalArgumentException("제목 또는 파일이 비어있습니다.");
            /* IllegalArgumentException은 메서드에 전달된 인자가 잘못되었음을 알리는 중요한 도구. 이를 통해 메서드가 예상한 입력 조건을 확실히 하고, 
            잘못된 인자가 전달되었을 때 적절한 조치를 취할 수 있음
            이 예외는 코드의 안정성과 명확성을 높이는 데 중요한 역할을 함 */
        } else { 
        String projectPath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\files"; // 저장할 경로 설정


        UUID uuid = UUID.randomUUID(); // 랜덤으로 이름을 만들어줌
        String fileName = uuid + "_" + file.getOriginalFilename();
        File saveFile = new File(projectPath, fileName); // 파일을 생성해줄건데 projectPath 경로에 넣어주고 이름을 넣어주는 것

        file.transferTo(saveFile); // 업로드된 파일을 saveFile 위치에 저장하는 기능
        board.setFilename(fileName);
        board.setFilepath("/files/" + fileName); // 서버에서는 스태틱밑에 경로 부분만 적어도 접근이 가능
        }

        board.setAuthor(currentUser);

        boardRepository.save(board); // JpaRepository.save 메서드는 엔터티를 데이터베이스에 저장하는 데 사용

        return 1; 
    } 

    public void update(Board board, MultipartFile file) throws Exception {
        
        if(file.isEmpty() || board.getTitle().isEmpty()) {
            throw new IllegalArgumentException("제목 또는 파일이 비어있습니다.");
            /* IllegalArgumentException은 메서드에 전달된 인자가 잘못되었음을 알리는 중요한 도구. 이를 통해 메서드가 예상한 입력 조건을 확실히 하고, 
            잘못된 인자가 전달되었을 때 적절한 조치를 취할 수 있음
            이 예외는 코드의 안정성과 명확성을 높이는 데 중요한 역할 */
        } else { 
        String projectPath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\files"; // 저장할 경로 설정


        UUID uuid = UUID.randomUUID(); // 랜덤으로 이름을 만들어줌
        String fileName = uuid + "_" + file.getOriginalFilename();
        File saveFile = new File(projectPath, fileName); // 파일을 생성해줄건데 projectPath 경로에 넣어주고 이름을 넣어주는 것

        file.transferTo(saveFile); // 업로드된 파일을 saveFile 위치에 저장하는 기능
        board.setFilename(fileName);
        board.setFilepath("/files/" + fileName); // 서버에서는 스태틱밑에 경로 부분만 적어도 접근이 가능
        }

        boardRepository.save(board); // JpaRepository.save 메서드는 엔터티를 데이터베이스에 저장하는 데 사용.
    } 

    // 게시글 리스트 처리
    @Cacheable(value = "boardListCache") // 캐시에 boardRepository.findAll(pageable)를 저장해놓고 그 캐시를 Redis에 저장
    public Page<Board> boardList(Pageable pageable) {
        return boardRepository.findAll(pageable); // 엔티티 클래스에 대한 모든 데이터를 조회, 반환값은 List 형태이며, T는 엔티티 클래스 모든 데이터를 조회하므로, 대용량의 데이터를 조회할 때는 성능 이슈가 발생할 수 있음.
    }  // pageable을 넘겨주면 페이지 정보와 사이즈 정보를 넘김

    // 특정 게시글 불러오기
    public Board boardView(Integer id) {
        return boardRepository.findById(id).get(); /* 
        boardRepository.findById(id).get();은 다음과 같은 역할을 함:

        주어진 id에 해당하는 Board 엔터티를 데이터베이스에서 조회
        조회 결과를 Optional로 감싸서 반환
        Optional에서 실제 엔터티 객체를 꺼내기 위해 .get()을 호출 */
    }

    // 특정 게시물 삭제
    public void boardDelete(Integer id) {
        boardRepository.deleteById(id); // 데이터 베이스에서 아예 삭제
    }

    public Page<Board> boardSearchList(String searchKeyword, Pageable pageable) {
        return boardRepository.searchByTitle(searchKeyword, pageable);
            
    }

    public String registerUser(User user) {

    try{
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);    
        return "등록이 완료되었습니다."; // 성공 메시지

    }   catch(Exception e) {
        e.printStackTrace();
        return "등록에 실패하였습니다."; // 실패 메시지
    
    }
}
    public boolean existsByLoginId(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Transactional
    public boolean likeLogic(Integer boardId, User currentUser) {
        Board board = boardRepository.findById(boardId) .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (likeRepository.existsByBoardAndUser(board, currentUser)) {
            likeRepository.deleteByBoardAndUser(board, currentUser);
            board.setLikeCount(board.getLikeCount() - 1);
            boardRepository.save(board);
            return false;
        } else {
            Likes like = new Likes();
           like.setBoard(board);
           like.setUser(currentUser);
           likeRepository.save(like); // 좋아요 저장

           board.setLikeCount(board.getLikeCount() + 1); // 좋아요 수 증가
           boardRepository.save(board);
           return true;
        }

    

    }

    public Board getBoardbyId(Integer boardId) {
        return boardRepository.findById(boardId) .orElseThrow(() -> new IllegalArgumentException("해당 게시물을 찾을 수 없습니다"));
    }

    public int getLikeCount(Integer boardId) {
        Board board = boardRepository.findById(boardId) .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
        return board.getLikeCount();
    }

    public boolean isLikedByUser(Integer boardId, User currentUser) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        boolean exists = likeRepository.existsByBoardAndUser(board, currentUser);
        System.out.println("좋아요 상태 확인 - boardId : " + boardId + ", userId " + currentUser.getId() + " => " + exists);
        return exists;
    }
}
