package com.ss.heartlinkapi.like.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.ss.heartlinkapi.comment.entity.CommentEntity;
import com.ss.heartlinkapi.post.entity.PostEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;

import lombok.Data;

@Entity
@Data
@Table(name = "likes")
@EntityListeners(AuditingEntityListener.class)
public class LikeEntity {
	@Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long likeId;					// 좋아요 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity userId;				// 회원 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id")
	private PostEntity postId;				// 게시글 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comment_id")
	private CommentEntity commentId;		// 댓글 아이디
	
	@CreatedDate
	@Column(name = "created_at", nullable= false, updatable = false)
	private LocalDateTime createdAt;			// 생성일시
	
	
	@Override
	public String toString() {
	    return "LikeEntity{" +
	            "likeId=" + likeId +
	            ", userId=" + userId +
	            ", commentId=" + (commentId != null ? commentId.getCommentId() : "null") + // null 체크 추가
	            '}';
	}


}
