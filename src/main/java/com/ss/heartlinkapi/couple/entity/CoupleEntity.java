package com.ss.heartlinkapi.couple.entity;

import com.ss.heartlinkapi.user.entity.UserEntity;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "couple")
public class CoupleEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long coupleId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "users1_id", nullable = false)
    private UserEntity user1;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "users2_id", nullable = false)
    private UserEntity user2;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;
    @Column(name = "anniversary_date")
    private LocalDate anniversaryDate = null;
    @Column(name = "breakup_date")
    private LocalDate breakupDate = null;
    @Column(name = "match_count")
    private Integer matchCount = 0;
    // 피드 공개 여부 체크(false = 전체공개, true = 팔로워한테만 공개)
    @Column(name = "is_private")
    private Boolean isPrivate = false;
}
