package com.ss.heartlinkapi.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class TextMessageDTO {
    private Long msgRoomId;
    private Long senderId;
    private String content;
}

//  텍스트 메시지인 경우 컨트롤러에 받아온 값을 임시 저장할 때 쓰이는 DTO