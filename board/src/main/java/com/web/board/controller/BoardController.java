package com.web.board.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.web.board.config.CustomUserDetails;
import com.web.board.config.JwtTokenProvider;
import com.web.board.entity.Board;
import com.web.board.entity.User;
import com.web.board.repository.UserRepository;
import com.web.board.service.BoardService;

@Controller
public class BoardController {
 
    @Autowired
    private BoardService boardService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/")
    public String home() {
        return "redirect:/board/list";
    }

    
    @GetMapping("/board/write")
    public String boardWriteForm() {
        
        return "boardwrite";
    }
    @CacheEvict(value = "boardListCache", allEntries = true)
    @PostMapping("/board/writepro") 
    public String boardWritePro(Board board, RedirectAttributes redirectAttributes, @RequestParam("file") MultipartFile file, @RequestHeader(value= "Authorization", required = false) String token) throws Exception { 
        try { 
            if(token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                String username = jwtTokenProvider.getUsername(jwtToken);
                User currentUser = userRepository.findByLoginId(username).orElse(null);
                if(currentUser != null) {
                    boardService.write(board, file, currentUser);
                    return "redirect:/board/list";
                }
            }
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다");
            return "redirect:/login";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "서버 오류가 발생하였습니다.");
            return "redirect:/board/write";  // redirectAttributes.addFlashAttribute를 사용하면 리다이렉트 이후 /write 페이지에서 "errorMessage" 속성을 사용할 수 있음
        }  
      }        // String message = (result > 0) ? "글 작성이 완료되었습니다" : "글 작성에 실패하였습니다";
        // String searchUrl = (result > 0) ? "board/list" : "board/write";
        // 기존 model로 넘겨서 프론트단에서 redirect를 처리하면 리다이렉트가 클라이언트 측에서 발생하기 때문에, 사용자는 추가적인 네트워크 왕복 시간을 경험할 수 있고, 서버에서 바로 리다이렉트하는 것보다 약간 비효율적
        // model.addAttribute("message", message);
        // model.addAttribute("searchUrl", searchUrl);

    @GetMapping("/board/list")
    public String boardList(Model model, @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                            String searchKeyword, @RequestHeader(value = "Authorization", required = false) String token) {
        // sort : 정렬 기준 direction : 정렬 순서

        if(token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            String username = jwtTokenProvider.getUsername(jwtToken);
            String nickname = jwtTokenProvider.getNickname(jwtToken);
        
        if(username != null) {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("loginId", username);
            model.addAttribute("nickname", nickname);
        }
        } else {
            model.addAttribute("isLoggedIn", false);
        }
            
        Page<Board> list = (searchKeyword == null || searchKeyword.trim().isEmpty())
            ? list = boardService.boardList(pageable) : boardService.boardSearchList(searchKeyword, pageable); 
      

        
        int nowPage = list.getPageable().getPageNumber() + 1;
        int startPage = Math.max(nowPage - 4, 1); // 둘을 비교하여 더 높은 값을 반환
        int endPage = Math.min(nowPage + 5, list.getTotalPages()); 
    
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("list", list);
        
        return "boardlist";
    }

    @CacheEvict(value = "boardListCache", allEntries = true)
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

    @CacheEvict(value = "boardListCache", allEntries = true)
    @PostMapping("/board/update/{id}")
    public String boardUpdate(@PathVariable("id") Integer id, Board board, MultipartFile file) throws Exception {

    Board boardTemp = boardService.boardView(id);
    boardTemp.setTitle(board.getTitle()); // html 폼에서 사용자가 입력한 정보를 얻어와서 수정
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

    @PostMapping("/board/like/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> likePost(@PathVariable("id") Integer boardId, @RequestHeader(value = "Authorization", required = false) String token) {
    
        Map<String, Object> response = new HashMap<>();
        boolean isLiked = false;

        if(token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            String username = jwtTokenProvider.getUsername(jwtToken);
            User currentUser = userRepository.findByLoginId(username).orElse(null);

            if(currentUser != null) {
                boolean isLikedBefore = boardService.isLikedByUser(boardId, currentUser);
                response.put("isLikedBefore", isLikedBefore);

                isLiked = boardService.likeLogic(boardId, currentUser);
                response.put("isLiked", isLiked);
            }
        } else {
            response.put("isLiked", false);
            response.put("isLikedBefore", false);
        }
           int likeCount = boardService.getLikeCount(boardId);
           response.put("likeCount", likeCount);

           return ResponseEntity.ok(response);   
         }

    @GetMapping("/board/{id}")
    public String getBoardDetails(@PathVariable("id") Integer boardId, Model model, @RequestHeader(value = "Authorization", required = false) String token) { 
        Board board = boardService.getBoardbyId(boardId);
        model.addAttribute("board", board);

        if(token != null && token.startsWith("Bearer ")) {
            
            String jwtToken = token.substring(7);
            String username = jwtTokenProvider.getUsername(jwtToken);
            String nickname = jwtTokenProvider.getNickname(jwtToken);

            if(username != null) {
                User currentUser = userRepository.findByLoginId(username).orElse(null);
                if(currentUser != null) {
                    boolean isAuthor = board.getAuthor() != null && board.getAuthor().getId().equals(currentUser.getId());

                    if(isAuthor) {
                        model.addAttribute("currentUser", currentUser);
                    }

                    boolean isLikedBefore12 = boardService.isLikedByUser(boardId, currentUser);
                    model.addAttribute("isLikedBefore12", isLikedBefore12);
                    model.addAttribute("nickname", nickname);
                }
            }
        } else {
            model.addAttribute("isLikedBefore", false);
        }
        return "boardview";
    }

    
}
    

