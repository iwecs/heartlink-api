package com.ss.heartlinkapi.report.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "report")
@EntityListeners(AuditingEntityListener.class)
public class ReportEntity {
	@Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long reportId;					// 신고 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id")
	private PostEntity postId;				// 게시글 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comment_id")
	private CommentEntity commentId;		// 댓글 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private UserEntity userId;				// 회원 아이디
	
	@Column(name = "reason")
	private String reason;					// 신고 사유

	@Enumerated(EnumType.STRING)
	private Status status;					// 상태
	
	@CreatedDate
	@Column(name = "created_at", nullable= false, updatable = false)
	private Timestamp createdAt;			// 생성일시
}
