package com.ss.heartlinkapi.user.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.*;

import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name="users")
public class UserEntity {	
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long userId; // 기본키 회원id
    
    @Column(name = "login_id", length = 30)
	private String loginId; // 일반 로그인 회원 아이디
    
    @Column(unique = true, length = 100)
    private String email; // 이메일(중복체크)
    
    private String password; // 비밀번호(8~16자이내 특수문자/영어/숫자 모두 포함)
    
    @Column(unique = true, nullable = false,length = 20)
    private String phone; // 전화번호(문자인증)  
    
    @Column(length = 20)
    private String name; // 이름 
    
    @Column(length = 1)
    private char gender; // 성별 남:M, 여:F
    
    @Enumerated(EnumType.STRING)
    private Role role; // 역할

    @ToString.Exclude
    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<SocialEntity> socialAccounts; // 소셜 엔티티 목록
    
    @Column(name = "couple_code", length = 6)
    private String coupleCode;	// 커플 코드
    
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt; // 생성일시
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일시
    
}
