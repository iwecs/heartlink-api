package com.ss.heartlinkapi.comment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ss.heartlinkapi.comment.dto.CommentDTO;
import com.ss.heartlinkapi.comment.dto.CommentUpdateDTO;
import com.ss.heartlinkapi.comment.service.CommentService;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.user.entity.UserEntity;

@RestController
@RequestMapping("/comment")
public class CommentController {
	
	private final CommentService commentService;
	
	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}
	
	// 댓글 작성
	@PostMapping("/{postId}/reply")
	public ResponseEntity<?> writeComment(@PathVariable Long postId, @RequestBody CommentDTO commentDTO, @AuthenticationPrincipal CustomUserDetails userDetails){
		
		UserEntity user = userDetails.getUserEntity();
		
		commentDTO.setPostId(postId);
		
		try {
			commentService.writeComment(commentDTO, user);
			
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} catch (Exception e) {
			e.printStackTrace();
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 작성 중 오류가 발생했습니다.");
		}
		
	}
	
	
	
	// 댓글 삭제
	@DeleteMapping("/{commentId}/reply/delete")
	public ResponseEntity<?> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal CustomUserDetails user) {
	    Long userId = user.getUserId();

	    try {
	        commentService.deleteComment(commentId, userId);
	        return ResponseEntity.ok("댓글 삭제 완료");
	    } catch (IllegalArgumentException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제 중 오류가 발생했습니다.");
	    }
	}
	
	// 댓글 수정
	@PutMapping("/{commentId}")
	public ResponseEntity<?> updateComment(@PathVariable Long commentId, @RequestBody CommentUpdateDTO updateDTO, @AuthenticationPrincipal CustomUserDetails user){
		
		Long userId = user.getUserId();
		
		try {
			commentService.updateComment(commentId, userId, updateDTO);
			return ResponseEntity.ok("댓글 수정 완료");
	    } catch (IllegalArgumentException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 수정 중 오류가 발생했습니다.");
	    }
	}

	

}
