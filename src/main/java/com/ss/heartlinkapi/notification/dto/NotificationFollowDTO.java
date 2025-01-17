package com.ss.heartlinkapi.notification.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class NotificationFollowDTO {
    private String url;
    private String message;
}
