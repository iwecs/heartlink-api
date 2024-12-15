package com.ss.heartlinkapi.comment.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ss.heartlinkapi.contentLinktag.entity.ContentLinktagEntity;
import com.ss.heartlinkapi.like.entity.LikeEntity;
import com.ss.heartlinkapi.mention.entity.MentionEntity;
import com.ss.heartlinkapi.post.entity.PostEntity;
import com.ss.heartlinkapi.report.entity.ReportEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;

import lombok.Data;

@Entity
@Data
@Table(name = "comment")
@EntityListeners(AuditingEntityListener.class)
public class CommentEntity {
	@Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long commentId;					// 댓글 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private PostEntity postId;				// 게시글 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private CommentEntity parentId;			// 부모 댓글 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity userId;				// 회원 아이디
	
	@Column(name = "content", nullable= false)
	private String content;					// 댓글 내용
	
	@CreatedDate
	@Column(name = "created_at", nullable= false, updatable = false)
	private LocalDateTime createdAt;			// 작성 시간
	
	@LastModifiedDate
	@Column(name = "update_at")
	private LocalDateTime updatedAt;			// 수정 시간
	
	@Column(name = "like_count", nullable = false)
	private int likeCount = 0;					// 댓글 좋아요 수
	
	
	// Cascade 처리
	@OneToMany(mappedBy = "commentId", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	@JsonIgnoreProperties({"commentId"})
	private List<ReportEntity> reports;
	
	@OneToMany(mappedBy = "commentId", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	@JsonIgnoreProperties({"commentId"})
    private List<LikeEntity> likes;
	
	@OneToMany(mappedBy = "parentId", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	@JsonIgnoreProperties({"parentId"})
	private List<CommentEntity> children;

	@OneToMany(mappedBy = "commentId", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	@JsonIgnoreProperties({"commentId"})
	private List<MentionEntity> mentions;
	
	@OneToMany(mappedBy = "commentId", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	@JsonIgnoreProperties({"commentId"})
	private List<ContentLinktagEntity> commentLinktags;
	
	
	@Override
	public String toString() {
	    return "CommentEntity{" +
	            "commentId=" + commentId +
	            ", content='" + content + '\'' +
	            ", likes=" + (likes != null ? likes.size() : 0) + 
	            '}';
	}


}
