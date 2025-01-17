package com.ss.heartlinkapi.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SaveMsgDTO {
    private Long msgRoomId;
    private Long senderId;
    private String content;
    private String emoji;
    private String imageUrl;
    private LocalDateTime messageTime;
    private boolean isRead;

}

//  메세지 저장에 쓰이는 DTO