package com.ss.heartlinkapi.notification.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)

public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sender_user_id")
    private Long senderUserId;

    @Column(name = "reciever_user_id")
    private Long recieverUserId;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(name = "user_img")
    private String userImg;

    @Column(name = "message")
    private String message;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "created_date")
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(name = "link")
    private String link;

}

/*
* userId는 외래키이므로 joinColumn 사용해 연결
* type enum 타입으로 설정
* builder 타입으로 간결성과 가독성 Up
* @CreatedDate 어노테이션 사용으로 자동으로 시간이 삽입
* */
