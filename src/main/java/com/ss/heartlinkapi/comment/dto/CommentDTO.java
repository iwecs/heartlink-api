package com.ss.heartlinkapi.comment.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
	
    private Long commentId;     // 댓글 아이디
    private Long postId;        // 게시글 아이디
    private Long parentId;      // 부모 댓글 아이디
    private Long userId;      // 회원 아이디
    private String content;      // 댓글 내용
    private LocalDateTime createdAt; // 작성 시간
    private LocalDateTime updatedAt; // 수정 시간
    
    private String loginId;		// 좋아요를 누른 사용자 loginId
    private String profileImg;	// 프로필 이미지
    private List<String> mentionedLoginIds;   // 댓글에 태그된 사용자들의 loginId 목록
    private List<Long> mentionedUserIds;      // 댓글에 태그된 사용자들의 userId 목록
    
    // 댓글 좋아요 상태
    private boolean isLiked;
}
