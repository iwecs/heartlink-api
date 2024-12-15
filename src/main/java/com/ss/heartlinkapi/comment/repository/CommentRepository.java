package com.ss.heartlinkapi.comment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ss.heartlinkapi.comment.entity.CommentEntity;
import com.ss.heartlinkapi.post.entity.PostEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;

public interface CommentRepository extends JpaRepository<CommentEntity, Long>{

	// 게시글 댓글 보기 (신고된 댓글 제외)
    @Query("SELECT c FROM CommentEntity c " +
           "WHERE c.postId = :postId AND c NOT IN (SELECT r.commentId FROM ReportEntity r WHERE r.userId.id = :userId)")
    List<CommentEntity> findByPostIdAndNotReported(@Param("postId") PostEntity postId, @Param("userId") Long userId);

    // 댓글 삭제
	CommentEntity findByCommentIdAndUserId_UserId(Long commentId, Long userId);
	
	/************* 유저로 댓글 수 조회 **************/
	int countByUserId(UserEntity user);
	
}
