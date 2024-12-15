package com.ss.heartlinkapi.block.entity;

import java.sql.Timestamp;

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

import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;

import lombok.Data;

@Entity
@Data
@Table(name = "block")
@EntityListeners(AuditingEntityListener.class)
public class BlockEntity {
	@Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long blockId;				// 차단 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "blocker_id", nullable = false)
	private UserEntity blockerId;		// 차단한 회원 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "blocked_id", nullable = false)
	private UserEntity blockedId;		// 차단된 회원 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "couple_id")
	private CoupleEntity coupleId;		// 차단된 커플 아이디
	
	@CreatedDate
	@Column(name = "created_at", nullable= false, updatable = false)
	private Timestamp createdAt;		// 생성일시

}
