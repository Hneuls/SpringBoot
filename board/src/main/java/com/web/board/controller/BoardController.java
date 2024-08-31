package com.web.board.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.web.board.config.CustomUserDetails;
import com.web.board.entity.Board;
import com.web.board.entity.User;
import com.web.board.service.BoardService;

// 예외처리는 컨트롤러가 맡아서 처리하게 한다.
@Controller
public class BoardController {
 
    @Autowired
    private BoardService boardService;

    @GetMapping("/")
    public String home() {
        return "redirect:/board/list";
    }

    
    @GetMapping("/board/write") //localhost:8080/board/write 주소로 접속하면 boardwrite 페이지를 보여줌
    public String boardWriteForm() {
        
        return "boardwrite";
    }
    @PostMapping("/board/writepro") // 파일과 제목을 넣어야만 하는 넣지않으면 오류메시지
    public String boardWritePro(Board board, RedirectAttributes redirectAttributes, @RequestParam("file") MultipartFile file, Principal principal) throws Exception { 
        try { 
            CustomUserDetails userDetails = (CustomUserDetails) ((Authentication) principal).getPrincipal();
            User currentUser = userDetails.getUser();
            boardService.write(board, file, currentUser);
            return "redirect:/board/list";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage()); 
            return "redirect:/board/write";  // redirectAttributes.addFlashAttribute를 사용하면 리다이렉트 이후 /write 페이지에서 "errorMessage" 속성을 사용할 수 있음
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "서버 오류가 발생하였습니다."); 
            return "redirect:/board/write";
        }
    }        // String message = (result > 0) ? "글 작성이 완료되었습니다" : "글 작성에 실패하였습니다";
        // String searchUrl = (result > 0) ? "board/list" : "board/write";
        // 기존 model로 넘겨서 프론트단에서 redirect를 처리하면 리다이렉트가 클라이언트 측에서 발생하기 때문에, 사용자는 추가적인 네트워크 왕복 시간을 경험할 수 있습니다. 서버에서 바로 리다이렉트하는 것보다 약간 비효율적
        // model.addAttribute("message", message);
        // model.addAttribute("searchUrl", searchUrl);

    @GetMapping("/board/list")
    public String boardList(Model model, @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                            String searchKeyword, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        // sort : 정렬 기준 direction : 정렬 순서

            Page<Board> list = null;
            
        if(searchKeyword == null || searchKeyword.trim().isEmpty()) {
            list = boardService.boardList(pageable); // 전체 결과
        } else {
            list = boardService.boardSearchList(searchKeyword, pageable); 
        }

        
        int nowPage = list.getPageable().getPageNumber() + 1;
        int startPage = Math.max(nowPage - 4, 1); // 둘을 비교하여 더 높은 값을 반환
        int endPage = Math.min(nowPage + 5, list.getTotalPages()); 
    
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("list", list);
        
        model.addAttribute("isLoggedIn", customUserDetails != null);
        if (customUserDetails != null) {
            model.addAttribute("loginId", customUserDetails.getLoginId());
            model.addAttribute("nickname", customUserDetails.getNickname());
        } 
        return "boardlist";
    }
 
    @GetMapping("/board/view") // localhost:8080/board/view?id=1 < id 매개변수에 전달하기 위해 url을 변경 
    public String boardView(Model model, @RequestParam Integer id, Principal principal) { // html에 넘겨줄 땐 model 사용

// Principal 객체는 현재 인증된 사용자에 대한 정보를 담고 있는 객체로 로그인한 사용자 정보를 담고 있습니다
// Authentication 객체는 Spring Security에서 현재 인증 정보를 담고 있습니다. 이 객체는 로그인한 사용자, 사용자의 권한, 인증 상태 등의 정보를 제공합니다.
// Principal을 Authentication으로 변환하는 이유는 Authentication이 Principal보다 더 많은 인증 관련 정보를 포함하고 있기 때문입니다.
        Board board = boardService.boardView(id);
        CustomUserDetails userDetails = (CustomUserDetails)((Authentication) principal).getPrincipal();
        User currentUser = userDetails.getUser();

        boolean isAuthor = board != null && board.getAuthor() != null && 
                        currentUser != null && board.getAuthor().getId().equals(currentUser.getId());

        if(isAuthor) {
        model.addAttribute("board", board); // 매개변수 id에 해당하는 필드(title, content)를 다 가져옴
        model.addAttribute("currentUser", currentUser);

        return "boardview";
        } else 
        model.addAttribute("board", board);
        return "boardview2";
    }

    @PostMapping("/board/delete") //delete url을 지정하면 service단의 id 삭제 밑 redirect 완료
    public String boardDelete(@RequestParam Integer id) {
        boardService.boardDelete(id);
        
        return "redirect:/board/list";
    }

    @GetMapping("/board/modify/{id}") // id부분이 pathVariable에 인식이 돼서 Integer형태의 id로 들어감
    public String boardModify(Model model, @PathVariable("id") Integer id) {

        model.addAttribute("board", boardService.boardView(id));

        return "boardmodify";
    }

    @PostMapping("/board/update/{id}")
    public String boardUpdate(@PathVariable("id") Integer id, Board board, MultipartFile file) throws Exception {

    Board boardTemp = boardService.boardView(id);
    boardTemp.setTitle(board.getTitle()); // html 폼에서 사용자가 입력한 정보를 얻어와서 수정함
    boardTemp.setContent(board.getContent());

    boardService.update(boardTemp, file); // html 폼에서 file이 매개변수에 들어와서 사진이 업데이트됨
        return "redirect:/board/list";
    }

    @GetMapping("/board/register")
    public String showRegistrationForm(Model model, User user) {
        model.addAttribute("user", user); 
        return "registerPage"; // 회원 가입 페이지로 이동
    }

    @PostMapping("/board/register/go") // html에 중복 메시지를 처리하는 부분이있는데 if 부분에 또 처리하는 이유는 html이 정상적으로 작동하지 않을 때 "최종 점검"하는 것이다
    public String registerUser(@ModelAttribute("user") User user, BindingResult result, Model model) {
        // 사용자 등록 처리 로직
        boolean loginIdExists = boardService.existsByLoginId(user.getLoginId());
        boolean nicknameExists = boardService.existsByNickname(user.getNickname());

        if(loginIdExists) {
            model.addAttribute("message", "중복된 아이디입니다.");
        }
        
        if(nicknameExists) {
            model.addAttribute("message", "중복된 닉네임입니다");
        }

        if (loginIdExists || nicknameExists) {
            return "registerPage"; // 중복된 경우 등록 페이지로 돌아가기
        }    

        String message = boardService.registerUser(user);
        model.addAttribute("message", message);

        if ("등록이 완료되었습니다.".equals(message)) {
            return "redirect:/board/list"; // 성공 시 리스트 페이지로 이동
        } else {
            // 실패 시 입력한 값 유지 및 폼으로 돌아감
            return "registerPage";
        }
    }

    @GetMapping("/user/check-login-id") // 아이디 중복방지 메서드 (회원가입에서 중복체크 처리)
    public ResponseEntity<Map<String, Boolean>> checkLoginId(@RequestParam String loginId) {
        boolean exists = boardService.existsByLoginId(loginId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists); // exists라는 변수에 false 혹은 true 를 담아서
        return ResponseEntity.ok(response); // html로 보내주는 것 
}

    @GetMapping("/user/check-nickname") // 닉네임 중복방지 메서드 ( // )
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam String nickname) {
        boolean exists = boardService.existsByNickname(nickname);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
}

    @GetMapping("/login") // (security에서 처리)
    public String loginForm() {
        return "login";
    }

    

    
}
    

