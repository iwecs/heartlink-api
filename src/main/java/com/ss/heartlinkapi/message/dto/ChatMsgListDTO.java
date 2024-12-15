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
public class ChatMsgListDTO {
    private Long messageId;
    private Long senderId;
    private String content;
    private String emoji;
    private String imageUrl;
    private LocalDateTime lastMessageTime;
    private boolean isRead;

}

//  messageList에 담길 메세지를 저장할 DTO