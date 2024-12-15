package com.ss.heartlinkapi.search.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ss.heartlinkapi.linktag.entity.LinkTagEntity;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.post.dto.PostFileDTO;
import com.ss.heartlinkapi.post.dto.PostSearchDTO;
import com.ss.heartlinkapi.post.entity.PostEntity;
import com.ss.heartlinkapi.post.entity.PostFileEntity;
import com.ss.heartlinkapi.post.repository.PostFileRepository;
import com.ss.heartlinkapi.post.service.PostService;
import com.ss.heartlinkapi.search.service.SearchService;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.ProfileRepository;
import com.ss.heartlinkapi.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private PostFileRepository postFileRepository;
    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;
    // 유저 별 검색기록 확인
    @GetMapping("/history")
    public ResponseEntity<?> searchHistory(@AuthenticationPrincipal CustomUserDetails user){
        try{
            if(user == null){
                return ResponseEntity.badRequest().body("유저 아이디가 존재하지 않습니다.");
            }

            List<Map<String, Object>> historyList = searchService.findHistoryByUserId(user.getUserId());
            if(historyList == null){
                return ResponseEntity.ok().body("검색 기록이 없습니다.");
            }

            return ResponseEntity.ok().body(historyList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    // 유저, 태그, 피드 내용 별 검색 후 결과 반환
    @GetMapping("/keyword")
    public ResponseEntity<?> search(@RequestParam String keyword, @AuthenticationPrincipal CustomUserDetails user) {
        try {
            UserEntity userEntity = userRepository.findById(user.getUserEntity().getUserId()).orElse(null);

            if(keyword == null || keyword.isEmpty() || user == null) {
                return ResponseEntity.badRequest().body(null);
            }

            if (keyword.startsWith("@")) {
                System.out.println(keyword);
                UserEntity userResult = searchService.searchByUserId(keyword, user.getUserId());
                if(userResult == null) {
                    return ResponseEntity.ok("검색 결과가 없습니다.");
                }
                Map<String, Object> map = new HashMap<>();
                map.put("userId", userResult.getUserId());
                map.put("loginId", userResult.getLoginId());
                map.put("type", "id");
                map.put("img", profileRepository.findByUserEntity(userResult).getProfile_img());
                return ResponseEntity.ok(map);
            } else if (keyword.startsWith("&")) {
                LinkTagEntity tag = searchService.searchByTag(keyword, user.getUserId());
                if(tag == null) {
                    return ResponseEntity.ok("검색 결과가 없습니다.");
                }
                Map<String, Object> map = new HashMap<>();
                map.put("tagId", tag.getId());
                map.put("tagName", tag.getKeyword());
                map.put("type", "tag");
                return ResponseEntity.ok(map);
            } else {
                System.out.println("키워드 : "+keyword);
                List<PostEntity> post = searchService.searchByPost(keyword, user.getUserId());
                if(post == null) {
                    return ResponseEntity.ok("검색 결과가 없습니다.");
                }

                List<Map<String, Object>> postList = new ArrayList<>();
                for(int i=0; i<post.size(); i++) {
                    Map<String, Object> postMap = new HashMap<>();
                    for(int j=0; j<post.size(); j++) {
                        List<PostFileEntity> file = postFileRepository.findByPostId(post.get(i).getPostId());
                        postMap.put("img", file.get(0).getFileUrl());
                        postMap.put("id", post.get(i).getPostId());
                        postMap.put("content", post.get(i).getContent());
                        postMap.put("type", "post");
                    }
                    postList.add(i, postMap);
                }

                return ResponseEntity.ok(postList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 검색창과 함께 띄울 게시글 조회
    @Transactional
    @GetMapping("/getSearchPost")
    public ResponseEntity<?> getPostList(@AuthenticationPrincipal CustomUserDetails user, @RequestParam(required = false) Integer cursor, @RequestParam(defaultValue = "30") int limit, HttpServletResponse response) {
        try {
            if(user == null) {
                return ResponseEntity.badRequest().body(null);
            }
            Map<String, Object> postList = searchService.getPost(user, cursor, limit);
            return ResponseEntity.ok(postList);
        } catch (Exception e) {
            e.printStackTrace();
            if (!response.isCommitted()) {
                return ResponseEntity.internalServerError().build();
            }
            return ResponseEntity.internalServerError().build();

        }
    }

    // 언급 시 보여줄 아이디 리스트 (팔로우 우선+모든 아이디)
    @GetMapping("/mentionId")
    public ResponseEntity<?> mentionId(@AuthenticationPrincipal CustomUserDetails user) {
        try{
            if(user == null) {
                return ResponseEntity.badRequest().body(null);
            }

        List<Map<String, Object>> userList = searchService.mentionIdList(user.getUserEntity());
        return ResponseEntity.ok(userList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 링크태그 검색
    @GetMapping("/tag")
    public List<PostFileDTO> searchPost(@RequestParam String keyword){
    	return postService.searchPostByLinktag(keyword);
    }

    // 모든 게시글 조회
    @GetMapping("/allPosts")
    public ResponseEntity<?> allPosts(@AuthenticationPrincipal CustomUserDetails user){
        List<Map<String, Object>> result = searchService.findGroupByPostId();
        return ResponseEntity.ok(result);
    }

}
