package com.ss.heartlinkapi.post.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.heartlinkapi.bookmark.service.BookmarkService;
import com.ss.heartlinkapi.like.service.LikeService;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.post.dto.PostDTO;
import com.ss.heartlinkapi.post.dto.PostUpdateDTO;
import com.ss.heartlinkapi.post.service.PostService;
import com.ss.heartlinkapi.user.entity.Role;
import com.ss.heartlinkapi.user.entity.UserEntity;


@RestController
@RequestMapping("/feed")
public class PostController {
	
	private final PostService postService;
	private final LikeService likeService;
	private final BookmarkService bookmarkService;
	
	public PostController(PostService postService, LikeService likeService, BookmarkService bookmarkService) {
		this.postService = postService;
		this.likeService = likeService;
		this.bookmarkService = bookmarkService;
	}
	
	
	// 게시글 작성
	@PostMapping("/write")
	public ResponseEntity<?> writePost(
	        @RequestParam("post") String postJson, // JSON 문자열로 받음
	        @RequestParam("files") List<MultipartFile> files,
	        @AuthenticationPrincipal CustomUserDetails userDetails) throws JsonMappingException, JsonProcessingException {
	    ObjectMapper objectMapper = new ObjectMapper();
	    PostDTO postDTO = objectMapper.readValue(postJson, PostDTO.class);

	    
	    UserEntity user = userDetails.getUserEntity();
	    
	    // 첨부파일이 없을 때 예외
	    if (files == null || files.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("첨부파일이 최소 1개 이상 포함되어야 합니다.");
	    }

	    try {
	        postService.savePost(postDTO, files, user);
	        return ResponseEntity.status(HttpStatus.CREATED).build();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 작성 중 오류가 발생하였습니다.");
	    }
	}

	// 피드 조회
	@GetMapping("")
	public ResponseEntity<?> getFollowingPublicPosts(
	        @AuthenticationPrincipal CustomUserDetails user,
	        @RequestParam(required = false) Integer cursor,
	        @RequestParam(defaultValue = "50") int limit) {

		// 커플 해지한 사용자는 접근 불가
//		if (user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals(Role.ROLE_SINGLE.name()))) {
//	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
//	                .body("커플을 해지한 사용자는 접근할 수 없습니다.");
//	    }
		
	    Long userId = user.getUserId();
	    
	    // 팔로우하는 게시물 조회 (커서 페이징)
	    Map<String, Object> followingPosts = postService.getPublicPostByFollowerId(userId, cursor, limit);

	    // 비팔로우 및 신고되지 않은 게시물 조회 (커서 페이징)
	    Map<String, Object> nonFollowedPosts = postService.getNonFollowedAndNonReportedPosts(userId, cursor, limit);

	    // 응답 데이터 생성
	    Map<String, Object> response = new HashMap<>();
	    response.put("followingPosts", followingPosts.get("data")); // 현재 페이지의 팔로우 게시물
	    response.put("nextFollowingCursor", followingPosts.get("nextCursor")); // 다음 커서
	    response.put("hasNextFollowing", followingPosts.get("hasNext")); // 다음 페이지 여부

	    response.put("nonFollowedPosts", nonFollowedPosts.get("data")); // 현재 페이지의 비팔로우 게시물
	    response.put("nextNonFollowedCursor", nonFollowedPosts.get("nextCursor")); // 다음 커서
	    response.put("hasNextNonFollowed", nonFollowedPosts.get("hasNext")); // 다음 페이지 여부

	    return ResponseEntity.ok().body(response);
	}




	
	
	// 게시글 상세보기
	@GetMapping("/details/{postId}")
	public ResponseEntity<PostDTO> getPostWithComments(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails user){
		
		Long userId = user.getUserId();
		
		PostDTO postDTO = postService.getPostById(postId, userId);
		
		return ResponseEntity.ok(postDTO);
	}
	
	// 내가 누른 좋아요 목록 조회
	@GetMapping("/like")
	public ResponseEntity<?> getLikePostFilesByUserId(@AuthenticationPrincipal CustomUserDetails user,
																	@RequestParam(required = false) Integer cursor,
																	@RequestParam(defaultValue = "50") int limit) {
		
		Long userId = user.getUserId();
		Map<String, Object> postFiles = likeService.getPostFilesByUserId(userId, cursor, limit); 				   
	    
	    return ResponseEntity.ok(postFiles);
	}
	
	// 내가 누른 북마크 목록 조회
	@GetMapping("/bookmark")
	public ResponseEntity<?> getBokkmarkPostFilesByUserId(@AuthenticationPrincipal CustomUserDetails user,
																	@RequestParam(required = false) Integer cursor,
																	@RequestParam(defaultValue = "50") int limit) {
		
		Long userId = user.getUserId();
		
		Map<String, Object> postFiles = bookmarkService.getBookmarkPostFilesByUserId(userId, cursor, limit);
		
		return ResponseEntity.ok(postFiles);
		
	}
	
	
	// 사용자와 사용자의 커플 게시글 목록 조회
	@GetMapping("/couple/{userId}")
	public ResponseEntity<?> getCouplePostFiles(
			@AuthenticationPrincipal CustomUserDetails user,
	        @PathVariable Long userId,
	        @RequestParam(required = false) Integer cursor,
	        @RequestParam(defaultValue = "50") int limit) {

		// 로그인한 사용자의 userId
		Long currentUserId = user.getUserId();
		
	    Map<String, Object> postFiles = postService.getPostFilesByUserId(currentUserId, userId, cursor, limit);
	    
	    return ResponseEntity.ok(postFiles);
	}

	
	// 내 게시글 삭제
	@DeleteMapping("/{postId}/delete")
	public ResponseEntity<?> deletePost(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails user){
		Long userId = user.getUserId();
		
		postService.deleteMyPost(postId, userId);
		
		return ResponseEntity.ok("게시글 삭제 완료");
		
	}
	
	// 모든 게시글 삭제
	@DeleteMapping("/user")
	public ResponseEntity<?> deleteAllPostsByUser(@AuthenticationPrincipal CustomUserDetails user){
		Long userId = user.getUserId();
		
		postService.deleteAllPostByUser(userId);
		return ResponseEntity.ok("모든 게시글 삭제 완료");
	}
	
	// 게시글 수정
	@PutMapping("/{postId}/update")
	public ResponseEntity<?> updatePost(
			@PathVariable Long postId,
		    @RequestBody PostUpdateDTO postUpdateDTO,
		    @AuthenticationPrincipal CustomUserDetails user){
		
		Long userId = user.getUserId();
		
		try {
			postService.updatePost(postId, userId, postUpdateDTO);
			return ResponseEntity.ok("게시글 수정 완료");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	



}
