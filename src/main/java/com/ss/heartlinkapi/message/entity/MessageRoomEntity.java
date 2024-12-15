package com.ss.heartlinkapi.message.entity;


import com.ss.heartlinkapi.notification.entity.Type;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "message_room")
public class MessageRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "user1_id")
    private Long user1Id;
    @Column(name = "user2_id")
    private Long user2Id;
    @Enumerated(EnumType.STRING)
    private MsgRoomType msgRoomType;
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
