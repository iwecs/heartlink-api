package com.ss.heartlinkapi.message.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BlockUserCheckDTO {
    private Long userId;
    private Long blockUserId;
}

//  block유저인지 체크할 때 쓰이는 DTO
