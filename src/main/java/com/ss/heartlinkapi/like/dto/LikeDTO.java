package com.ss.heartlinkapi.like.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeDTO {
	
    private Long likeId;        // 좋아요 ID
    private Long userId;        // 좋아요를 누른 사용자 ID
    private String loginId;		// 좋아요를 누른 사용자 loginId
    private String profileImg;	// 프로필 이미지
    private Long postId;        // 좋아요가 눌린 게시글 ID
    private Long commentId;     // 좋아요가 눌린 댓글 ID
    private LocalDateTime createdAt;   // 생성일시
}
