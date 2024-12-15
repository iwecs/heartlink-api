package com.ss.heartlinkapi.message.entity;


import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "message")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "msg_room_id")
    private Long msgRoomId;
    @Column(name = "sender_id")
    private Long senderId;
    @Column(name = "content", length = 500)
    private String content;
    @Column(name = "img_url")
    private String imgUrl;
    @Column(name = "is_read")
    private boolean isRead;
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "emoji", length = 10)
    private String emoji;

}
