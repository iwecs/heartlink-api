package com.ss.heartlinkapi.message.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FriendDTO {
    private Long friendId;
    private String friendName;
    private String friendImg;
}
