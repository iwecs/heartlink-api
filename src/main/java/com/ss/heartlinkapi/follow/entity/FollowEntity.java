package com.ss.heartlinkapi.follow.entity;

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

import com.ss.heartlinkapi.user.entity.UserEntity;

import lombok.Data;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@Table(name = "follow")
public class FollowEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
    private Long followId;				// 팔로우 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "follower_id")
	private UserEntity follower;		// 팔로우하는 회원 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "following_id")
	private UserEntity following;		// 팔로우되는 회원 아이디
	
	private boolean status = true;				// 팔로우 상태(false -> 대기중)
	
	@CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt; 	// 생성일시

}
