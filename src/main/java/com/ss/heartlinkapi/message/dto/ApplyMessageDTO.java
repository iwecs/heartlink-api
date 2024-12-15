package com.ss.heartlinkapi.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ApplyMessageDTO {
    private Long userId;
    private Long applyId;
    private String type;
}

//  비공개 유저에게 메시지 보낼때 담을 요청이 저장되는 DTO