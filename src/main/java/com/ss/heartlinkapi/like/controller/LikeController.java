package com.ss.heartlinkapi.like.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ss.heartlinkapi.like.dto.LikeDTO;
import com.ss.heartlinkapi.like.service.LikeService;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;


@RestController
@RequestMapping("/like")
public class LikeController {
	
	private final LikeService likeService;
	
	public LikeController(LikeService likeService) {
		this.likeService = likeService;
	}
	
	// 게시글의 좋아요 목록 조회
    @GetMapping("/{postId}/users")
    public ResponseEntity<List<LikeDTO>> getLikesByPostId(@PathVariable Long postId) {
        List<LikeDTO> likes = likeService.getLikesByPostId(postId);
        return ResponseEntity.ok(likes);
    }

    // 댓글의 좋아요 목록 조회
    @GetMapping("/comment/{commentId}/users")
    public ResponseEntity<List<LikeDTO>> getLikesByCommentId(@PathVariable Long commentId) {
        List<LikeDTO> likes = likeService.getLikesByCommentId(commentId);
        return ResponseEntity.ok(likes);
    }
    
    // 좋아요 증감
    @PostMapping("/toggle")
    public ResponseEntity<String> toggleLike(
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) Long commentId,
            @AuthenticationPrincipal CustomUserDetails user) {

        Long userId = user.getUserId();
        boolean likeAdded = likeService.addOrRemoveLike(postId, userId, commentId);

        if (likeAdded) {
            return ResponseEntity.ok("좋아요 추가");
        } else {
            return ResponseEntity.ok("좋아요 삭제");
        }
    }

}
