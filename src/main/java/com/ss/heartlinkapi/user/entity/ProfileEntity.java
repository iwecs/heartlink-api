package com.ss.heartlinkapi.user.entity;

import java.time.LocalDateTime;

import javax.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "profile")
public class ProfileEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
	private Long profileId;
	
	@OneToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "user_id")
	private UserEntity userEntity;
	
	private String nickname;
	
	private String profile_img;
	
	@Column(length = 20)
	private String bio;
	
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt; // 생성일시
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일시
	
}
